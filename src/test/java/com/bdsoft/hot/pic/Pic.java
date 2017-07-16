package com.bdsoft.hot.pic;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import com.bdsoft.utils.Constant;
import com.sun.image.codec.jpeg.JPEGCodec;
import com.sun.image.codec.jpeg.JPEGEncodeParam;
import com.sun.image.codec.jpeg.JPEGImageEncoder;

public class Pic {

	public static String TEST_NAME = "111510514792";

	public static void main(String[] args) throws Exception {
		String path = "e:/data/pic/";
		String bpath = "e:/data/pic/big/";
		File dir = new File(path);
		if (dir.exists() && dir.isDirectory()) {
			File[] files = dir.listFiles();
			int sum = files.length;
			System.out.println("总共待处理图片 " + sum);
			int index = 0;
			for (File file : files) {
				index++;
				if (file.isDirectory()) {
					System.out.println("目录过滤...");
					continue;
				}
				String name = file.getName();
				toBig(path, name, bpath);
				System.out.println("处理图片 " + index + "of" + sum);
				Thread.sleep(150);
			}
		}

		// t130723(null, null, null);
		// t13072302(null, null, null);
	}

	public static void toBig(String path, String name, String bpath)
			throws Exception {
		// 指定最大输出的宽和高
		int w = Integer.parseInt(Constant.pic_big_width);
		int h = Integer.parseInt(Constant.pic_big_height);
		String ext = Constant.ext; // 图片输出格式
		float per = Constant.per; // 图片压缩品质
		String file = path + name;
		String bfile = bpath + name;

		FileInputStream fi = new FileInputStream(new File(file));
		Image src = javax.imageio.ImageIO.read(fi);
		int src_w = src.getWidth(null);
		int src_h = src.getHeight(null);
		BufferedImage srcImage = new BufferedImage(src_w, src_h,
				BufferedImage.TYPE_INT_RGB);
		Graphics2D g = srcImage.createGraphics();
		g.setColor(Color.white);
		g.drawImage(src, 0, 0, src_w, src_h, Color.white, null);
		g.dispose();
		src = srcImage;
		int new_w = 0;
		int new_h = 0;
		int base = Math.max(src_w, src_h);
		double p = (base * 1.00) / (w * 1.00);
		if (src_w > w || src_h > h) {
			new_w = (int) Math.round(src_w / p);
			new_h = (int) Math.round(src_h / p);
		} else {
			new_w = src_w;
			new_h = src_h;
		}
		BufferedImage newImage = new BufferedImage(new_w, new_h,
				BufferedImage.TYPE_INT_RGB);
		newImage.getGraphics().drawImage(
				src.getScaledInstance(new_w, new_h, Image.SCALE_SMOOTH), 0, 0,
				null);
		FileOutputStream newimage = new FileOutputStream(bfile);
		JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(newimage);
		JPEGEncodeParam jep = JPEGCodec.getDefaultJPEGEncodeParam(newImage);
		jep.setQuality(per, true);
		encoder.encode(newImage, jep);
		newimage.close();
		System.out.println("over");
	}

	public static void t13072302(String path, String name, String bpath)
			throws Exception {
		// 指定最大输出的宽和高
		int w = Integer.parseInt(Constant.pic_big_width);
		int h = Integer.parseInt(Constant.pic_big_height);
		String ext = Constant.ext; // 图片输出格式
		float per = Constant.per; // 图片压缩品质
		String fpath = "E:/data/pic/zol/";
		if (path != null) {
			fpath = path;
		}
		String fname = TEST_NAME;
		String file = fpath + fname + ext;
		if (name != null) {
			file = fpath + name;
		}
		String ofile = fpath + fname + "-b2" + ext;
		if (bpath != null) {
			ofile = bpath + name;
		}

		// 读取原图进行处理
		FileInputStream fi = new FileInputStream(new File(file));
		Image src = javax.imageio.ImageIO.read(fi);
		// 原图宽和高
		int src_w = src.getWidth(null);
		int src_h = src.getHeight(null);

		BufferedImage srcImage = new BufferedImage(src_w, src_h,
				BufferedImage.TYPE_INT_RGB);
		// if (src_w > src_h) {// 宽>高 -- 宽+高
		// srcImage = new BufferedImage(src_w, src_h,
		// BufferedImage.TYPE_INT_RGB);
		// } else if (src_w < src_h) {// 宽<高 -- 高+高
		// // srcImage = new BufferedImage(src_h, src_h,
		// // BufferedImage.TYPE_INT_RGB);
		// srcImage = new BufferedImage(src_w, src_h,
		// BufferedImage.TYPE_INT_RGB);
		// } else {// 宽+高
		// srcImage = new BufferedImage(src_w, src_h,
		// BufferedImage.TYPE_INT_RGB);
		// }
		// 2D画图
		Graphics2D g = srcImage.createGraphics();
		g.setColor(Color.white);
		// if (src_w > src_h) {
		// g.fillRect(0, 0, src_w, src_w);
		// g.drawImage(src, 0, 0, src_w, src_h, Color.white, null);
		// } else if (src_w < src_h) {
		// g.fillRect(0, 0, src_h, src_h);
		// // g.drawImage(src, (src_h - src_w) / 2, 0, src_w, src_h,
		// // Color.white,
		// // null);
		// g.drawImage(src, 0, 0, src_w, src_h, Color.white, null);
		// } else {
		// // g.fillRect(0,0,old_h,old_h);
		// g.drawImage(
		// src.getScaledInstance(src_w, src_h, Image.SCALE_SMOOTH), 0,
		// 0, null);
		// }
		g.drawImage(src, 0, 0, src_w, src_h, Color.white, null);
		g.dispose();

		src = srcImage;
		int new_w = 0;
		int new_h = 0;
		// double w2 = (src_w * 1.00) / (w * 1.00);
		// double h2 = (src_h * 1.00) / (h * 1.00);
		int base = Math.max(src_w, src_h);
		double p = (base * 1.00) / (w * 1.00);
		// 原图宽大于指定输出宽
		// if (src_w > w) {
		// // new_w = (int) Math.round(src_w / w2);
		// new_w = (int) Math.round(src_w / p);
		// } else {
		// new_w = src_w;
		// if (src_h > h ) {
		// new_w = (int) Math.round(src_w / p);
		// }
		// }
		// // 原图高大于指定输出高
		// if (src_h > h ) {
		// // new_h = (int) Math.round(src_h / h2);
		// new_h = (int) Math.round(src_h / p);
		// if(src_w > w){
		// new_h = (int) Math.round(src_h / p);
		// }
		// } else {
		// new_h = src_h;
		// if(src_w > w){
		// new_h = (int) Math.round(src_h / p);
		// }
		// }
		if (src_w > w || src_h > h) {
			new_w = (int) Math.round(src_w / p);
			new_h = (int) Math.round(src_h / p);
		} else {
			new_w = src_w;
			new_h = src_h;
		}
		BufferedImage newImage = new BufferedImage(new_w, new_h,
				BufferedImage.TYPE_INT_RGB);
		newImage.getGraphics().drawImage(
				src.getScaledInstance(new_w, new_h, Image.SCALE_SMOOTH), 0, 0,
				null);
		FileOutputStream newimage = new FileOutputStream(ofile); // 输出到文件流
		JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(newimage);
		JPEGEncodeParam jep = JPEGCodec.getDefaultJPEGEncodeParam(newImage);
		jep.setQuality(per, true);
		encoder.encode(newImage, jep);
		newimage.close();
		System.out.println("over");
	}

	public static void t130723(String path, String name, String bpath)
			throws Exception {
		// 指定最大输出的宽和高
		int w = Integer.parseInt(Constant.pic_big_width);
		int h = Integer.parseInt(Constant.pic_big_height);
		String ext = Constant.ext; // 图片输出格式
		float per = Constant.per; // 图片压缩品质
		String fpath = "E:/data/pic/zol/";
		if (path != null) {
			fpath = path;
		}
		String fname = TEST_NAME;
		String file = fpath + fname + ext;
		if (name != null) {
			file = fpath + name;
		}
		String ofile = fpath + fname + "-b" + ext;
		if (bpath != null) {
			ofile = bpath + name;
		}

		// 读取原图进行处理
		FileInputStream fi = new FileInputStream(new File(file));
		Image src = javax.imageio.ImageIO.read(fi);
		// 原图宽和高
		int src_w = src.getWidth(null);
		int src_h = src.getHeight(null);

		BufferedImage srcImage;
		if (src_w > src_h) {// 宽>高 -- 宽+宽
			// srcImage = new BufferedImage(src_w, src_w,
			// BufferedImage.TYPE_INT_RGB);
			srcImage = new BufferedImage(src_w, src_h,
					BufferedImage.TYPE_INT_RGB);
		} else if (src_w < src_h) {// 宽<高 -- 高+高
			srcImage = new BufferedImage(src_h, src_h,
					BufferedImage.TYPE_INT_RGB);
		} else {// 宽+高
			srcImage = new BufferedImage(src_w, src_h,
					BufferedImage.TYPE_INT_RGB);
		}
		// 2D画图
		Graphics2D g = srcImage.createGraphics();
		g.setColor(Color.white);
		if (src_w > src_h) {
			g.fillRect(0, 0, src_w, src_w);
			// Image, int x, int y, int width, int height, Color, ImageObserver
			// g.drawImage(src, 0, (src_w - src_h) / 2, src_w, src_h,
			// Color.white, null);
			g.drawImage(src, 0, 0, src_w, src_h, Color.white, null);
		} else if (src_w < src_h) {
			g.fillRect(0, 0, src_h, src_h);
			g.drawImage(src, (src_h - src_w) / 2, 0, src_w, src_h, Color.white,
					null);
		} else {
			// g.fillRect(0,0,old_h,old_h);
			g.drawImage(
					src.getScaledInstance(src_w, src_h, Image.SCALE_SMOOTH), 0,
					0, null);
		}
		g.dispose();

		src = srcImage;
		int new_w = 0;
		int new_h = 0;
		double w2 = (src_w * 1.00) / (w * 1.00);
		double h2 = (src_h * 1.00) / (h * 1.00);
		// 原图宽大于指定输出宽
		if (src_w > w) {
			new_w = (int) Math.round(src_w / w2);
		} else {
			new_w = src_w;
		}
		// 原图高大于指定输出高
		if (src_h > h) {
			new_h = (int) Math.round(src_h / h2);
		} else {
			new_h = src_h;
		}
		BufferedImage newImage = new BufferedImage(new_w, new_h,
				BufferedImage.TYPE_INT_RGB);
		newImage.getGraphics().drawImage(
				src.getScaledInstance(new_w, new_h, Image.SCALE_SMOOTH), 0, 0,
				null);
		FileOutputStream newimage = new FileOutputStream(ofile); // 输出到文件流
		JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(newimage);
		JPEGEncodeParam jep = JPEGCodec.getDefaultJPEGEncodeParam(newImage);
		jep.setQuality(per, true);
		encoder.encode(newImage, jep);
		newimage.close();
		System.out.println("over");
	}
}
