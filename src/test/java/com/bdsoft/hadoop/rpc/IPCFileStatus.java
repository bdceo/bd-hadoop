package com.bdsoft.hadoop.rpc;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Date;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.io.WritableFactories;
import org.apache.hadoop.io.WritableFactory;

public class IPCFileStatus implements Writable {

	private String filename;
	private long time;

	// 注册可序列化对象
	static {
		WritableFactories.setFactory(IPCFileStatus.class,
				new WritableFactory() {
					@Override
					public Writable newInstance() {
						return new IPCFileStatus();
					}
				});
	}

	public IPCFileStatus() {
	}

	public IPCFileStatus(String filename) {
		this.filename = filename;
		this.time = (new Date()).getTime();
	}

	@Override
	public void write(DataOutput out) throws IOException {
		Text.writeString(out, filename);
		out.writeLong(time);
	}

	@Override
	public void readFields(DataInput in) throws IOException {
		this.filename = Text.readString(in);
		this.time = in.readLong();

	}

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

	public long getTime() {
		return time;
	}

	public void setTime(long time) {
		this.time = time;
	}

	@Override
	public String toString() {
		return "File: " + filename + " Create at " + (new Date(time));
	}
}
