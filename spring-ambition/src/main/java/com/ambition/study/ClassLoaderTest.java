package com.ambition.study;

import com.sun.crypto.provider.DESKeyFactory;

import java.io.FileInputStream;
import java.lang.reflect.Method;

/**
 * @author Elewin
 * @date 2020-05-06 9:18 PM
 * @version 1.0
 * AiWays 爱驰汽车
 */
public class ClassLoaderTest {

	/**
	 * 加载 -> 验证 -> 准备【赋默认值】 -> 解析 -> 初始化【赋实际值】 -> 使用 -> 卸载
	 *
	 * 启动类加载器【BootStrap ClassLoader】：负责加载支撑JVM运行的位于JRE的lib目录下的核心类库，比如rt.jar等
	 * 扩展类加载器【Extension ClassLoader】：负责加载支撑JVM运行的位于JRE的lib目录下的ext扩展目录中的JAR类包
	 * 应用程序类加载器【Application ClassLoader】：负责加载ClassPath路径下的类包，主要就是加载用户写的类
	 * 自定义加载器【User ClassLoader】：负责加载用户自定义路径下的类包
	 *
	 * 双亲委派机制：
	 * 	    加载某个类时会先委托父加载器寻找目标类
	 * 		找不到再委托上层父加载器加载
	 * 		如果所有父加载器在自己的加载类路径下都找不到目标类
	 * 		则在自己的类加载路径中查找并载入目标类
	 *
	 * 为什么要设计双亲委派机制？
	 * 1.沙箱安全机制：自己写的 java.lang.String.class 类不会被加载，这样便可以防止核心 API 库被随意篡改
	 * 2.避免类的重复加载：当父亲已经加载了该类时，子类 ClassLoader 就没有必要再加载一次，保证被加载类的唯一性
	 * 类和加载类的类加载器保证了被加载类的唯一性，类加载器不同，即使是同样的类，也不相同
	 **/
	public static void main(String[] args) throws Exception {
		// 启动类加载器，C++ 语言实现的，所以打印不出来
		System.out.println(String.class.getClassLoader());// null
		// 扩展类加载器
		System.out.println(DESKeyFactory.class.getClassLoader().getClass().getName());// sun.misc.Launcher$ExtClassLoader
		// 应用程序类加载器
		System.out.println(ClassLoaderTest.class.getClassLoader().getClass().getName());// sun.misc.Launcher$AppClassLoader
		// 系统类加载器，一般是应用程序类加载器
		System.out.println(ClassLoader.getSystemClassLoader().getClass().getName());// sun.misc.Launcher$AppClassLoader

		MyClassLoader classLoader = new MyClassLoader("/var");
		Class clazz = classLoader.loadClass("com.ambition.entity.User");
		Object obj = clazz.newInstance();
		Method method = clazz.getDeclaredMethod("print", null);
		method.invoke(obj, null);
		// com.ambition.study.ClassLoaderTest$MyClassLoader
		System.out.println(clazz.getClassLoader().getClass().getName());
	}

	// 自己实现的类加载器
	public static class MyClassLoader extends ClassLoader {

		private String classPath;

		public MyClassLoader(String classPath) {
			super();
			this.classPath = classPath;
		}

		private byte[] loadByte(String name) throws Exception {
			name = name.replaceAll("\\.", "/");
			FileInputStream fis = new FileInputStream(classPath + "/" + name
					+ ".class");
			int len = fis.available();
			byte[] data = new byte[len];
			fis.read(data);
			fis.close();
			return data;
		}

		@Override
		protected Class<?> findClass(String name) throws ClassNotFoundException {
			try {
				byte[] data = loadByte(name);
				return defineClass(name, data, 0, data.length);
			}
			catch (Exception e) {
				e.printStackTrace();
				throw new ClassNotFoundException();
			}
		}

		/*protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
			synchronized (getClassLoadingLock(name)) {
				// First, check if the class has already been loaded
				Class<?> c = findLoadedClass(name);
				if (c == null) {
					// If still not found, then invoke findClass in order to find the class.
					long t1 = System.nanoTime();
					c = findClass(name);

					// this is the defining class loader; record the stats
					sun.misc.PerfCounter.getFindClassTime().addElapsedTimeFrom(t1);
					sun.misc.PerfCounter.getFindClasses().increment();
				}

				if (resolve) {
					resolveClass(c);
				}

				return c;
			}
		}*/

		/*@Override
		protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
			synchronized (getClassLoadingLock(name)) {
				*//**
				 * First, check if the class has already been loaded
				 * 首先，检查该类是否已经加载
				 **//*
				Class<?> c = findLoadedClass(name);
				// 没有加载
				if (c == null) {
					long t0 = System.nanoTime();
					try {
						// 有父加载器
						if (parent != null) {
							// 委托父加载器加载
							c = parent.loadClass(name, false);
						}
						// 没有父加载器了
						else {
							// 委托启动类加载器加载
							c = findBootstrapClassOrNull(name);
						}
					}
					catch (ClassNotFoundException e) {
						// ClassNotFoundException thrown if class not found
						// from the non-null parent class loader
					}

					// 父加载器都无法加载
					if (c == null) {
						*//**
						 * If still not found, then invoke findClass in order to find the class.
						 * 如果仍然找不到，则调用 findClass 以便找到该类
						 *
						 * 所以自定义的类加载器，只要重写 findClass 方法即可
						 * 如果要打破双亲委派机制，还需要重写 loadClass 方法
						 **//*
						long t1 = System.nanoTime();
						c = findClass(name);

						*//**
						 * this is the defining class loader; record the stats
						 * 这是定义类加载器； 记录统计数据
						 **//*
						sun.misc.PerfCounter.getParentDelegationTime().addTime(t1 - t0);
						sun.misc.PerfCounter.getFindClassTime().addElapsedTimeFrom(t1);
						sun.misc.PerfCounter.getFindClasses().increment();
					}
				}
				if (resolve) {
					resolveClass(c);
				}
				return c;
			}
		}*/

	}

}
