package com.hero.jcr.commandline;

import java.lang.management.ManagementFactory;
import java.sql.Timestamp;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import com.googlecode.sardine.Sardine;
import com.googlecode.sardine.SardineFactory;
import com.microsoft.azure.storage.blob.CloudBlobContainer;
import com.microsoft.azure.storage.blob.CloudBlockBlob;

public class DownloadThread extends Thread {

	private CloudBlobContainer containerURL;
	private String azureFilePath;
	private String jCRFilename;
	private String host;
	private Sardine sardine;
	private String path;
	private String user;
	private String password;

	public DownloadThread(CloudBlobContainer containerURL, String azureFilePath, String jCRFilename, String host,
			Sardine sardine2, String path, String user, String password) {
		this.containerURL = containerURL;
		this.azureFilePath = azureFilePath;
		this.jCRFilename = jCRFilename;
		this.host = host;
		this.sardine = sardine2;
		this.path = path;
		this.user = user;
		this.password = password;
	}

	@Override
	public void run() {
		try {
			System.out.println("Thread");
			byte[] data = getFileFromAzure(containerURL, azureFilePath);

			webDavPostAEM(data, jCRFilename, host, sardine, path, user, password);
			
			long time = ManagementFactory.getThreadMXBean().getThreadCpuTime(this.getId());
	        System.out.println("My thread " + this.getId() + " execution time: " + TimeUnit.MILLISECONDS.toSeconds(time));
	        Date date = new Date();
	        System.out.println(new Timestamp(date.getTime()));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static byte[] getFileFromAzure(CloudBlobContainer containerURL, String filename) throws Exception {

		CloudBlockBlob blockBlob = containerURL.getBlockBlobReference(filename);
		blockBlob.downloadAttributes();
		long fileByteLength = blockBlob.getProperties().getLength();
		byte[] fileContent = new byte[(int) fileByteLength];
		blockBlob.downloadToByteArray(fileContent, 0);

		return fileContent;

	}

	private static void webDavPostAEM(byte[] data, String filename, String host, Sardine sardine2, String path,
			String user, String password) {
		try {
			Sardine sardine = SardineFactory.begin(user, password);
			sardine.put(host + path + "/" + filename.replace(" ", "_"), data);
		} catch (Exception e) {
			System.out.println("failed to move file to JCR: " + filename + " : " + e.getMessage());
			e.printStackTrace();
		}

	}
}