package fr.olympa.api.module;

public class ClassLoader {

	//	public class OlympaClassLoader extends ClassLoader {
	//		public Class<?> load(Class<?> clazz) throws IOException {
	//			File file = new File(clazz.getProtectionDomain().getCodeSource().getLocation().getPath());
	//			InputStream is = OlympaClassLoader.class.getResourceAsStream(file.getAbsolutePath());
	//			ByteArrayOutputStream baos = new ByteArrayOutputStream();
	//			int b = -1;
	//			while ((b = is.read()) > -1)
	//				baos.write(b);
	//			return super.defineClass(file.getName(), baos.toByteArray(), 0, baos.size());
	//		}
	//	}

}