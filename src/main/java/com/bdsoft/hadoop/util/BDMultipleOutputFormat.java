package com.bdsoft.hadoop.util;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.io.WritableComparable;
import org.apache.hadoop.io.compress.CompressionCodec;
import org.apache.hadoop.io.compress.GzipCodec;
import org.apache.hadoop.mapreduce.OutputCommitter;
import org.apache.hadoop.mapreduce.RecordWriter;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.mapreduce.lib.output.FileOutputCommitter;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.ReflectionUtils;

/**
 * 自定义多文件输出
 */
public abstract class BDMultipleOutputFormat<K extends WritableComparable<?>, V extends Writable>
		extends FileOutputFormat<K, V> {

	private BDMultiRecordWriter writer = null;

	/**
	 * 指定输出文件名，子类需实现此方法：根据key或value指定输出文件名
	 * 
	 * @param key
	 *            reduce输出key
	 * @param value
	 *            reduce输出value
	 * @param conf
	 *            job配置文件
	 * @return
	 */
	protected abstract String genFileNmaeForOutput(K key, V value,
			Configuration conf);

	@Override
	public RecordWriter<K, V> getRecordWriter(TaskAttemptContext job)
			throws IOException, InterruptedException {
		if (writer == null) {
			writer = new BDMultiRecordWriter(job, getTaskOutputPath(job));
		}
		return writer;
	}

	/**
	 * 获取job的输出根路径
	 */
	private Path getTaskOutputPath(TaskAttemptContext job) throws IOException {
		Path workPath = null;

		OutputCommitter cmt = super.getOutputCommitter(job);

		if (cmt instanceof FileOutputCommitter) {
			workPath = ((FileOutputCommitter) cmt).getWorkPath();
		} else {
			Path outputPath = super.getOutputPath(job);
			if (outputPath == null) {
				throw new RuntimeException("job conf error： no output path ");
			}
			workPath = outputPath;
		}

		return workPath;
	}

	/**
	 * 自定义多文件输出器
	 */
	public class BDMultiRecordWriter extends RecordWriter<K, V> {

		private Path workPath = null;
		private TaskAttemptContext job = null;
		// 缓存多文件输出器
		private Map<String, RecordWriter<K, V>> writerCaches = null;

		public BDMultiRecordWriter(TaskAttemptContext job, Path workPath) {
			super();
			this.job = job;
			this.workPath = workPath;
			writerCaches = new HashMap<String, RecordWriter<K, V>>();
		}

		@Override
		public void write(K key, V value) throws IOException,
				InterruptedException {
			String baseName = genFileNmaeForOutput(key, value,
					job.getConfiguration());
			RecordWriter<K, V> rw = this.writerCaches.get(baseName);
			if (rw == null) {
				rw = newRecordWriter(job, baseName);
				this.writerCaches.put(baseName, rw);
			}

			rw.write(key, value);
		}

		private RecordWriter<K, V> newRecordWriter(TaskAttemptContext job,
				String baseName) throws IOException, InterruptedException {
			String separator = "\t";// 键值分隔符
			RecordWriter<K, V> arw = null;
			Configuration conf = job.getConfiguration();

			// 是否输出压缩
			if (FileOutputFormat.getCompressOutput(job)) {
				Class<? extends CompressionCodec> compCodec = FileOutputFormat
						.getOutputCompressorClass(job, GzipCodec.class);
				CompressionCodec codec = ReflectionUtils.newInstance(compCodec,
						conf);
				Path file = new Path(workPath, baseName
						+ codec.getDefaultExtension());
				FSDataOutputStream out = file.getFileSystem(conf).create(file,
						false);
				arw = new BDRecordWriter<>(new DataOutputStream(
						codec.createOutputStream(out)), separator);
			} else {
				// Resolve a child path against a parent path
				// Path parent, String child
				Path file = new Path(workPath, baseName);
				FSDataOutputStream out = file.getFileSystem(conf).create(file,
						false);
				arw = new BDRecordWriter<>(out, separator);
			}

			return arw;
		}

		@Override
		public void close(TaskAttemptContext context) throws IOException,
				InterruptedException {
			for (Entry<String, RecordWriter<K, V>> en : writerCaches.entrySet()) {
				en.getValue().close(context);
			}
			this.writerCaches.clear();
		}
	}

	/**
	 * 自定义输出器
	 */
	public static class BDRecordWriter<K, V> extends RecordWriter<K, V> {

		// 输出流
		DataOutputStream out;

		final byte[] kvSeparator; // 键值对分隔符
		static final byte[] newline;// 默认输出新的一行分隔

		static final String charset = "UTF-8";

		static {
			try {
				newline = "\n".getBytes(charset);
			} catch (Exception e) {
				throw new RuntimeException("charset error");
			}
		}

		public BDRecordWriter(DataOutputStream out) {
			this(out, "\t");
		}

		public BDRecordWriter(DataOutputStream out, String separator) {
			this.out = out;
			try {
				this.kvSeparator = separator.getBytes(charset);
			} catch (Exception e) {
				throw new RuntimeException("charset error");
			}
		}

		@Override
		public void write(K key, V value) throws IOException,
				InterruptedException {
			boolean knull = (key == null) || (key instanceof NullWritable);
			boolean vnull = (value == null) || (value instanceof NullWritable);

			if (knull && vnull) {
				return;
			}

			if (!knull) {
				writeObject(key); // key
			}
			if (!(knull || vnull)) {
				out.write(kvSeparator);// 分隔
			}
			if (!vnull) {
				writeObject(value);// value
			}
			out.write(newline);// 换行
		}

		/**
		 * 输出对象
		 */
		private void writeObject(Object o) throws IOException {
			if (o instanceof Text) {
				Text t = (Text) o;
				out.write(t.getBytes(), 0, t.getLength());
			} else {
				out.write(o.toString().getBytes(charset));
			}
		}

		/**
		 * 注意：在赋写的方法前，加了同步属性，表明多文件输出时容易出现并发问题
		 */
		@Override
		public synchronized void close(TaskAttemptContext context)
				throws IOException, InterruptedException {
			if (out != null) {
				out.close();
			}
		}

	}

}
