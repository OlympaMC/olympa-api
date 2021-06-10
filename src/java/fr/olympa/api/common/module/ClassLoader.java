package fr.olympa.api.common.module;

/**
 * L'idée ici de faire une method pour remplacer une class chargé par une même class avec une version plus récente
 * @author Tristiisch
 *
 */
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