package wb.android.storage;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.Nullable;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Enumeration;
import java.util.concurrent.Executors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

//There are a lot of helper methods here to make sure that no functionality is lost if a method is overwritten
// can be provided by Dagger (BaseAppModule)
public class StorageManager {

	private static final String TAG = "StorageManager";

	protected final DirectoryRoot root;

	private static SDCardFileManager _externalInstance = null;
	private static InternalStorageManager _internalInstance = null;

	protected StorageManager(DirectoryRoot root) {
		this.root = root;
	}

	public static StorageManager getInstance(Context context) {
		if (_externalInstance != null)
			return _externalInstance;

		_externalInstance = new SDCardFileManager(context);
		return _externalInstance;
	}

	public static final SDCardFileManager getExternalInstance(Context context) throws SDCardStateException {
		if (_externalInstance != null)
			return _externalInstance;
		_externalInstance = new SDCardFileManager(context);
		return _externalInstance;
	}

	public static final InternalStorageManager getInternalInstance(Context context) {
		if (_internalInstance != null)
			return _internalInstance;
		_internalInstance = new InternalStorageManager(context);
		return _internalInstance;
	}

	public static String GetRootPath() {
		if (_externalInstance != null)
			return _externalInstance.root.get().getAbsolutePath();
		else if (_internalInstance != null)
			return _internalInstance.root.get().getAbsolutePath();
		else
			return "";
	}

	public void initialize() {
		// Attempt to pre-fetch our root
		Executors.newSingleThreadExecutor().execute(new Runnable() {
			@Override
			public void run() {
				root.get();
			}
		});
	}

	public File getRoot() {
		return new File(root.get().getAbsolutePath());
	}

	// Returns the directory if successfully built. Returns null otherwise
	public File mkdir(final String name) {
		return mkdirHelper(root.get(), name);
	}

	public File mkdir(final File root, final String name) {
		return mkdirHelper(root, name);
	}

	private final File mkdirHelper(final File root, final String name) {
		final File dir = new File(root, name);
		if (!dir.exists()) {
			final boolean success = dir.mkdir();
			if (!success)
				return null;
		}
		return dir;
	}

	public File getFile(final String filename) {
		return new File(root.get(), filename);
	}

	public File getFile(final File root, final String filename) {
		return new File(root, filename);
	}

	/**
	 * Attempts to rename oldFile with name newName. It returns a new file if successful and the oldFile if not
	 * 
	 * @param oldFile
	 * @param newName
	 * @return
	 */
	public File rename(final File oldFile, final String newName) {
		final File file = this.getFile(oldFile.getParentFile(), newName);
		final boolean success = oldFile.renameTo(file);
		if (success)
			return file;
		else
			return oldFile;
	}

	/**
	 * Attempts to rename oldFile with name newName. It returns a new file if successful and the oldFile if not
	 *
	 * @param oldFile
	 * @param file
	 * @return
	 */
	public File rename(final File oldFile, final File file) {
		final boolean success = oldFile.renameTo(file);
		if (success)
			return file;
		else
			return oldFile;
	}

	public boolean delete(final String filename) {
		return deleteHelper(new File(root.get(), filename));
	}

	public boolean delete(final File dir, final String filename) {
		return deleteHelper(new File(dir, filename));
	}

	public boolean delete(final File file) {
		return deleteHelper(file);
	}

	private final boolean deleteHelper(final File file) {
		if (file == null || !file.exists())
			return true;
		if (!file.canWrite())
			return false;
		if (file.isDirectory() && file.listFiles().length > 0)
			return false;
		return renameThenDelete(file);
	}

	public boolean deleteRecursively(final File dir) {
		if (dir == null || !dir.exists())
			return true;
		if (!dir.canWrite())
			return false;
		if (dir.isDirectory()) {
			final File[] files = dir.listFiles();
			final int len = files.length;
			for (int i = 0; i < len; i++)
				deleteRecursively(files[i]);
			if (dir.listFiles().length > 0)
				return false;
		}
		return renameThenDelete(dir);
	}

	/**
	 * On some devices, you can get a EBUSY (Device or resource busy) when deleting. Renaming and then deleting can
	 * mitigate this according to SO.
	 * http://stackoverflow.com/questions/11539657/open-failed-ebusy-device-or-resource-busy
	 * 
	 * @param file
	 *            - the file to delete
	 * @return {@code true} if the delete succeeded. {@code false} if not
	 */
	private boolean renameThenDelete(final File file) {
		if (file == null || !file.exists()) {
			return true;
		}
		final File renamedFiled = this.rename(file, "delete.me." + file.hashCode());
		return renamedFiled.delete();
	}

	public boolean write(final String filename, final byte[] data) {
		return writeHelper(root.get(), filename, data);
	}

	public boolean write(final File dir, final String filename, final byte[] data) {
		return writeHelper(dir, filename, data);
	}

	private final boolean writeHelper(final File dir, final String filename, final byte[] data) {
		if (data == null || filename == null || dir == null)
			return false;
		String path = dir.toString();
		if (!path.endsWith(File.separator))
			path += File.separator;
		try (FileOutputStream fos = new FileOutputStream(path + filename)) {
			fos.write(data);
		} catch (IOException e) {
			return false;
		}
		return true;
	}

	public boolean write(final String filename, final String data) {
		return writeHelper(root.get(), filename, data);
	}

	public boolean write(final File dir, final String filename, final String data) {
		return writeHelper(dir, filename, data);
	}

	private final boolean writeHelper(final File dir, final String filename, final String data) {
		String path = dir.toString();
		if (!path.endsWith(File.separator))
			path += File.separator;
		try (FileOutputStream fos = new FileOutputStream(path + filename)) {
			fos.write(data.getBytes());
		} catch (IOException e) {
			return false;
		}
		return true;
	}

	public boolean writeBitmap(final File dir, final Bitmap bitmap, final String filename, final CompressFormat format, int quality) {
		return writeBitmapHelper(dir, bitmap, filename, format, quality);
	}

	private final boolean writeBitmapHelper(final File dir, final Bitmap bitmap, final String filename, final CompressFormat format, int quality) {
		String path = dir.toString();
		if (!path.endsWith(File.separator))
			path += File.separator;
		try (FileOutputStream fos = new FileOutputStream(path + filename)) {
			bitmap.compress(format, quality, fos);
		} catch (IOException e) {
			Log.e(TAG, e.toString());
			return false;
		}
		return true;
	}

	public boolean writeBitmap(final Uri imageUri, final Bitmap bitmap, final CompressFormat format, int quality) {
		if (imageUri == null || bitmap == null || format == null) {
			Log.e(TAG, "Invalid parameters - cannot supply null values to writeBitmap method");
			return false;
		}
		try (FileOutputStream fos = new FileOutputStream(imageUri.getPath())) {
			bitmap.compress(format, quality, fos);
		} catch (IOException e) {
			Log.e(TAG, e.toString());
			return false;
		}
		return true;
	}

	public byte[] read(final File f) {
		byte[] buffer;
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(f);
			buffer = new byte[(int) f.length()];
			fis.read(buffer);
			fis.close();
		}
		catch (FileNotFoundException e) {
			return null;
		}
		catch (IOException e) {
			buffer = null;
			return buffer;
		}
		finally {
			try {
				if (fis != null)
					fis.close();
			}
			catch (IOException e) {
				return null;
			}
		}
		return buffer;
	}

	public byte[] read(final InputStream is) {
		if (is == null)
			Log.d(TAG, "null");
		ByteArrayOutputStream os = null;
		byte[] bytes;
		try {
			os = new ByteArrayOutputStream();
			byte[] buffer = new byte[2048];
			int n = 0;
			while (-1 != (n = is.read(buffer))) {
				os.write(buffer, 0, n);
			}
			bytes = os.toByteArray();
			os.close();
		}
		catch (IOException e) {
			Log.e(TAG, e.toString());
			return null;
		}
		finally {
			try {
				if (os != null)
					os.close();
			}
			catch (IOException e) {
				Log.e(TAG, e.toString());
			}
		}
		return bytes;
	}

	public File changeExtension(File source, String newExtension) {
		try {
			if (!newExtension.startsWith("."))
				newExtension = "." + newExtension;
			String path = source.getCanonicalPath();
			path = path.substring(0, path.lastIndexOf('.'));
			path = path + newExtension;
			File destination = new File(path);
			if (source.renameTo(destination))
				return destination;
			else
				return null;
		}
		catch (IOException e) {
			return null;
		}
	}

	public boolean move(File source, File destination) {
		return source.renameTo(destination);
	}

	public boolean copy(File destination, boolean overwrite) throws IOException {
		return copyHelper(root.get(), destination, overwrite);
	}

	public boolean copy(File source, File destination, boolean overwrite) throws IOException {
		return copyHelper(source, destination, overwrite);
	}

	private boolean copyHelper(File source, File destination, boolean overwrite) throws IOException {
		if (source == null || destination == null || !source.exists()) {
			Log.e(TAG, "Invalid Inputs. Either the source/destination is null or the source does not exist");
			return false;
		}
		try {
			if (source.getCanonicalPath().equalsIgnoreCase(destination.getCanonicalPath())) {
				Log.e(TAG, "Invalid Inputs. The source File cannot have the same path as the destination File");
				return false;
			}
		}
		catch (IOException e) {
			throw e;
		}
		if (source.isDirectory()) {
			if (!destination.exists()) {
				if (!destination.mkdir()) {
					throw new IOException("Failed to create the directory: " + destination.getAbsolutePath());
				}
			}
			else {
				if (!destination.isDirectory()) {
					throw new IOException("Cannot copy a directory to a file");
				}
			}
			File[] files = listFilesAndDirectories(source);
			for (int i = 0; i < files.length; i++) {
				File sourceSub = files[i];
				File destSub = getFile(destination, sourceSub.getName());
				copy(sourceSub, destSub, overwrite);
			}
		}
		else {
			if (destination.exists() && !overwrite)
				return false;
			if (destination.isDirectory())
				destination = getFile(destination, source.getName());
			FileInputStream fis = null;
			FileOutputStream fos = null;
			try {
				fis = new FileInputStream(source);
				fos = new FileOutputStream(destination);
				byte[] buffer = new byte[8192];
				int read;
				while ((read = fis.read(buffer)) != -1) {
					fos.write(buffer, 0, read);
				}
			}
			catch (IOException e) {
				throw e;
			}
			finally {
				try {
					if (fis != null)
						fis.close();
				}
				catch (IOException e) {
					Log.e(TAG, e.toString());
				}
				try {
					if (fos != null)
						fos.close();
				}
				catch (IOException e) {
					Log.e(TAG, e.toString());
				}
			}
		}
		return true;
	}

	/**
	 * The caller is responsible for closing the InputStream
	 * 
	 * @param is
	 * @param destination
	 * @param overwrite
	 * @return
	 * @throws IOException
	 */
	public boolean copy(InputStream is, File destination, boolean overwrite) throws IOException {
		if (is == null || destination == null) {
			Log.e(TAG, "Invalid Inputs. Either the source/destination is null");
			return false;
		}
		if (destination.exists() && !overwrite)
			return false;
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(destination);
			byte[] buffer = new byte[1024];
			int read;
			while ((read = is.read(buffer)) != -1) {
				fos.write(buffer, 0, read);
			}
			fos.close();
		}
		catch (IOException e) {
			throw e;
		}
		finally {
			try {
				if (fos != null)
					fos.close();
			}
			catch (IOException e) {
				Log.e(TAG, e.toString());
			}
		}
		return true;
	}

	public File[] listFilesAndDirectories() {
		return root.get().listFiles();
	}

	public File[] listFilesAndDirectories(final File root) {
		return root.listFiles();
	}

	public File[] listFiles() {
		return listFilesHelper(root.get());
	}

	public File[] listFiles(final File root) {
		return listFilesHelper(root);
	}

	private final File[] listFilesHelper(final File root) {
		FileFilter filesFilter = new FileFilter() {
			public final boolean accept(File file) {
				return !file.isDirectory();
			}
		};
		return this.list(root, filesFilter);
	}

	public File[] listDirs() {
		return listDirsHelper(root.get());
	}

	public File[] listDirs(final File root) {
		return listDirsHelper(root);
	}

	private final File[] listDirsHelper(final File root) {
		FileFilter dirsFilter = new FileFilter() {
			public final boolean accept(File file) {
				return file.isDirectory();
			}
		};
		return this.list(root, dirsFilter);
	}

	public File[] list(final FileFilter filter) {
		return root.get().listFiles(filter);
	}

	public File[] list(final File root, final FileFilter filter) {
		return root.listFiles(filter);
	}

	public File[] list(final String extension) {
		return listHelper(root.get(), extension);
	}

	public File[] list(final File root, final String extension) {
		return listHelper(root, extension);
	}

	private final File[] listHelper(final File root, final String extension) {
		FileFilter ff = new FileFilter() {
			@Override
			@SuppressLint("DefaultLocale")
			public boolean accept(File pathname) {
				if (pathname.getName().toLowerCase().endsWith(extension))
					return true;
				else
					return false;
			}
		};
		return list(root, ff);
	}

	public Bitmap getMutableMemoryEfficientBitmap(File file) {
		return getMutableMemoryEfficientBitmapHelper(file, Bitmap.Config.RGB_565, 1024);
	}

	private final Bitmap getMutableMemoryEfficientBitmapHelper(File file, Bitmap.Config config, int maxDimension) {
		FileInputStream fis = null;
		try {
			// Decode image size
			BitmapFactory.Options o = new BitmapFactory.Options();
			o.inJustDecodeBounds = true;
			fis = new FileInputStream(file);
			BitmapFactory.decodeStream(fis, null, o);

			int scale = 1;
			while (o.outWidth / scale / 2 >= maxDimension && o.outHeight / scale / 2 >= maxDimension)
				scale *= 2;

			// Decode with inSampleSize
			BitmapFactory.Options o2 = new BitmapFactory.Options();
			o2.inSampleSize = scale;
			o2.inPurgeable = true;
			o2.inInputShareable = true;
			o2.inDither = true;
			o2.inPreferredConfig = config;
			return BitmapFactory.decodeFile(file.getAbsolutePath(), o2).copy(config, true);
		}
		catch (FileNotFoundException e) {
			return null;
		}
		finally {
			if (fis != null)
				StorageManager.closeQuietly(fis);
		}
	}

	public boolean isExternal() {
		return (this instanceof SDCardFileManager);
	}

	public FileOutputStream getFOS(String filename) throws FileNotFoundException {
		return getFOSHelper(root.get(), filename);
	}

	public FileOutputStream getFOS(File dir, String filename) throws FileNotFoundException {
		return getFOSHelper(dir, filename);
	}

	private final FileOutputStream getFOSHelper(File dir, String filename) throws FileNotFoundException {
		String path = dir.toString();
		if (!path.endsWith(File.separator))
			path += File.separator;
		return new FileOutputStream(path + filename);
	}

	public File zipBuffered(int buffer, FileFilter filter) {
		return zipBufferedHelper(root.get(), buffer, filter);
	}

	public File zipBuffered(File inputDir, int buffer) {
		return zipBufferedHelper(inputDir, buffer, null);
	}

	// Uses a buffer to conserve memory
	private File zipBufferedHelper(File inputDir, int buffer, FileFilter filter) {
		// What if file exists
		if (!inputDir.isDirectory()) {
			Log.e(TAG, "The input is not a directory");
			return null;
		}
		File zipFile = (inputDir.getParentFile() != null) ? getFile(inputDir.getParentFile(), inputDir.getName() + ".zip") : getFile(inputDir.getName() + ".zip");
		ZipOutputStream zipStream = null;
		try {
			zipStream = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(zipFile)));
			zipBufferedRecursively(inputDir, inputDir, zipStream, buffer, filter);
			zipStream.close();
		}
		catch (IOException e) {
			Log.e(TAG, e.toString());
			return null;
		}
		finally {
			closeQuietly(zipStream);
		}
		return zipFile;
	}

	private void zipBufferedRecursively(File file, File base, ZipOutputStream zipStream, int buffer, FileFilter filter) throws IOException {
		if (file.isDirectory()) {
			File[] files;
			if (filter == null)
				files = listFilesAndDirectories(file);
			else
				files = list(file, filter);
			for (int i = 0; i < files.length; i++) {
				zipBufferedRecursively(files[i], base, zipStream, buffer, filter);
			}
		}
		else {
			ZipEntry entry = new ZipEntry(file.getPath().substring(base.getPath().length() + 1));
			zipStream.putNextEntry(entry);
			BufferedInputStream reader = null;
			try {
				reader = new BufferedInputStream(new FileInputStream(file), buffer);
				int count;
				byte[] bytes = new byte[buffer];
				while ((count = reader.read(bytes, 0, buffer)) != -1) {
					zipStream.write(bytes, 0, count);
				}
				zipStream.write(bytes);
				reader.close();
			}
			finally {
				closeQuietly(reader);
			}
		}
	}

	public File zip(File inputDir) {
		return zipBuffered(inputDir, 8192);
	}

	public boolean unzip(File zip, boolean overwrite) {
		return unzipHelper(zip, root.get(), 8192, overwrite);
	}

	private boolean unzipHelper(File zip, File extractTo, int buffer, boolean overwrite) {
		InputStream in = null;
		BufferedOutputStream out = null;
		ZipFile archive = null;
		try {
			archive = new ZipFile(zip);
			Enumeration<? extends ZipEntry> e = archive.entries();
			while (e.hasMoreElements()) {
				ZipEntry entry = (ZipEntry) e.nextElement();
				File file;
				if ("receipts.db-wal".equals(entry.getName())) {
					// A hack to make this work for WAL files
					// TODO: Generalize this behavior whenever we re-write this class
					file = new File(extractTo, "receipts_backup.db-wal");
				} else {
					file = new File(extractTo, entry.getName());
				}
				if (entry.getName().endsWith(".zip"))
					continue; // Fix in the future. Currently, just ignore nested .zip files
				File parent = file.getParentFile();
				if (!parent.exists()) {
					parent.mkdirs();
				}
				if (entry.isDirectory()) {
					file.mkdirs();
				}
				else {
					if (!overwrite && file.exists()) {
						continue;
					}
					delete(file); // Remove the existing file before overwriting
					in = archive.getInputStream(entry);
					out = new BufferedOutputStream(new FileOutputStream(file));
					byte[] bytes = new byte[buffer];
					int read;
					while (-1 != (read = in.read(bytes))) {
						out.write(bytes, 0, read);
					}
					in.close();
					out.close();
				}
			}
		}
		catch (IOException e) {
			Log.e(TAG, e.toString());
			return false;
		}
		finally {
			try {
				if (in != null)
					in.close();
				if (out != null)
					out.close();
				if (archive != null)
					archive.close();
			}
			catch (IOException e) {
				Log.e(TAG, e.toString());
			}
		}
		return true;
	}

	public static String getMD5Checksum(InputStream is) {
		byte[] buffer = new byte[1024];
		MessageDigest complete;
		try {
			complete = MessageDigest.getInstance("MD5");
			try {
				int numRead;
				do {
					numRead = is.read(buffer);
					if (numRead > 0)
						complete.update(buffer, 0, numRead);
				}
				while (numRead != -1);
			}
			catch (IOException e) {
				Log.e(TAG, e.toString());
				return null;
			}
			byte[] b = complete.digest();
			StringBuilder builder = new StringBuilder();
			for (int i = 0; i < b.length; i++) {
				builder.append(Integer.toString((b[i] & 0xff) + 0x100, 16).substring(1));
			}
			return builder.toString();
		}
		catch (NoSuchAlgorithmException e) {
			Log.e(TAG, e.toString());
			return null;
		}
	}

	public static String getMD5Checksum(File file) {
		FileInputStream fis = null;
		byte[] buffer = new byte[1024];
		MessageDigest complete;
		try {
			complete = MessageDigest.getInstance("MD5");
			try {
				fis = new FileInputStream(file);
				int numRead;
				do {
					numRead = fis.read(buffer);
					if (numRead > 0)
						complete.update(buffer, 0, numRead);
				}
				while (numRead != -1);
				fis.close();
			}
			catch (IOException e) {
				Log.e(TAG, e.toString());
				return null;
			}
			finally {
				try {
					if (fis != null)
						fis.close();
				}
				catch (IOException e) {
					Log.e(TAG, e.toString());
				}
			}

			byte[] b = complete.digest();
			StringBuilder builder = new StringBuilder();
			for (int i = 0; i < b.length; i++) {
				builder.append(Integer.toString((b[i] & 0xff) + 0x100, 16).substring(1));
			}
			return builder.toString();
		}
		catch (NoSuchAlgorithmException e) {
			Log.e(TAG, e.toString());
			return null;
		}
	}

	public void print() {
		printHelper(root.get(), "> ");
	}

	private void printHelper(File file, String indent) {
		if (file.isDirectory()) {
			Log.d(TAG, indent + File.separatorChar + file.getName() + " -- " + file.getAbsolutePath());
			for (File child : file.listFiles())
				printHelper(child, "  " + indent);
		}
		else {
			Log.d(TAG, indent + file.getName());
		}
	}

	/**
	 * Returns the file extension. Returns null if the file is invalid (null, nameless, directory)
	 * 
	 * @param file
	 *            - the file to get the extension from
     * @deprecated in favor of {UriUtils}
	 * @return the extension as a string
	 */
    @Nullable
    @Deprecated
	public static String getExtension(File file) {
		if (file == null || TextUtils.isEmpty(file.getName())) {
			return null;
		}
		String name = file.getName();
		int index = name.lastIndexOf('.');
		if (index == -1) {
			return null;
		}
		else {
			return name.substring(index + 1);
		}
	}

	// -----------------------------------------------------------------------
	// Copied from Apache IO Utils
	// -----------------------------------------------------------------------

	/**
	 * Unconditionally close an <code>Reader</code>.
	 * <p>
	 * Equivalent to {@link Reader#close()}, except any exceptions will be ignored. This is typically used in finally
	 * blocks.
	 * 
	 * @param input
	 *            the Reader to close, may be null or already closed
	 */
	public static void closeQuietly(Reader input) {
		try {
			if (input != null) {
				input.close();
			}
		}
		catch (IOException ioe) {
			// ignore
		}
	}

	/**
	 * Unconditionally close a <code>Writer</code>.
	 * <p>
	 * Equivalent to {@link Writer#close()}, except any exceptions will be ignored. This is typically used in finally
	 * blocks.
	 * 
	 * @param output
	 *            the Writer to close, may be null or already closed
	 */
	public static void closeQuietly(Writer output) {
		try {
			if (output != null) {
				output.close();
			}
		}
		catch (IOException ioe) {
			// ignore
		}
	}

	/**
	 * Unconditionally close an <code>InputStream</code>.
	 * <p>
	 * Equivalent to {@link InputStream#close()}, except any exceptions will be ignored. This is typically used in
	 * finally blocks.
	 * 
	 * @param input
	 *            the InputStream to close, may be null or already closed
	 */
	public static void closeQuietly(InputStream input) {
		try {
			if (input != null) {
				input.close();
			}
		}
		catch (IOException ioe) {
			// ignore
		}
	}

	/**
	 * Unconditionally close an <code>OutputStream</code>.
	 * <p>
	 * Equivalent to {@link OutputStream#close()}, except any exceptions will be ignored. This is typically used in
	 * finally blocks.
	 * 
	 * @param output
	 *            the OutputStream to close, may be null or already closed
	 */
	public static void closeQuietly(OutputStream output) {
		try {
			if (output != null) {
				output.close();
			}
		}
		catch (IOException ioe) {
			// ignore
		}
	}

}