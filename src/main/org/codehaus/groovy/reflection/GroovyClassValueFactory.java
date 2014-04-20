package org.codehaus.groovy.reflection;

import java.lang.reflect.Constructor;

import org.codehaus.groovy.reflection.GroovyClassValue.ComputeValue;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

class GroovyClassValueFactory {
	private static final Constructor groovyClassValueConstructor;

	static {
		boolean javaLangClassValueExists;
		Class groovyClassValueClass;
		try{
			Class.forName("java.lang.ClassValue");
			javaLangClassValueExists = true;
		}catch(ClassNotFoundException e){
			javaLangClassValueExists = false;
		}
		if(javaLangClassValueExists){
			groovyClassValueClass =  getGroovyClassValueJava7();
		}else{
			groovyClassValueClass = GroovyClassValuePreJava7.class;
		}
		try{
			groovyClassValueConstructor = groovyClassValueClass.getConstructor(ComputeValue.class);
		}catch(Exception e){
			throw new RuntimeException(e); // this should never happen, but if it does, let it propagate and be fatal
		}
	}

	private static Class getGroovyClassValueJava7(){
		// Groovy must compile on Java 6, so there can't be a direct reference to java.lang.ClassValue
		// So use ASM to dynamically create the class that extends the java.lang.ClassValue abstract class
		// This code was made by running ASMifier on this class:
		/*
		 * public class GroovyClassValueJava7<T> extends ClassValue<T> implements GroovyClassValue<T> {
		 *	 private final ComputeValue<T> computeValue;
		 *	 public GroovyClassValueJava7(ComputeValue<T> computeValue){
		 *     this.computeValue = computeValue;
		 *   }
		 *   @Override
		 *   protected T computeValue(Class<?> type) {
		 *     return computeValue.computeValue(type);
		 *   }
		 * }
		 */
		ClassWriter cw = new ClassWriter(0);
		FieldVisitor fv;
		MethodVisitor mv;
		AnnotationVisitor av0;

		cw.visit(Opcodes.V1_6, Opcodes.ACC_PUBLIC | Opcodes.ACC_SUPER, "org/codehaus/groovy/reflection/GroovyClassValueJava7", "<T:Ljava/lang/Object;>Ljava/lang/ClassValue<TT;>;Lorg/codehaus/groovy/reflection/GroovyClassValue<TT;>;", "java/lang/ClassValue", new String[] { "org/codehaus/groovy/reflection/GroovyClassValue" });

		cw.visitInnerClass("org/codehaus/groovy/reflection/GroovyClassValue$ComputeValue", "org/codehaus/groovy/reflection/GroovyClassValue", "ComputeValue", Opcodes.ACC_PUBLIC + Opcodes.ACC_STATIC + Opcodes.ACC_ABSTRACT + Opcodes.ACC_INTERFACE);

		{
			fv = cw.visitField(Opcodes.ACC_PRIVATE + Opcodes.ACC_FINAL, "computeValue", "Lorg/codehaus/groovy/reflection/GroovyClassValue$ComputeValue;", "Lorg/codehaus/groovy/reflection/GroovyClassValue$ComputeValue<TT;>;", null);
			fv.visitEnd();
		}
		{
			mv = cw.visitMethod(Opcodes.ACC_PUBLIC, "<init>", "(Lorg/codehaus/groovy/reflection/GroovyClassValue$ComputeValue;)V", "(Lorg/codehaus/groovy/reflection/GroovyClassValue$ComputeValue<TT;>;)V", null);
			mv.visitCode();
			mv.visitVarInsn(Opcodes.ALOAD, 0);
			mv.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/ClassValue", "<init>", "()V", false);
			mv.visitVarInsn(Opcodes.ALOAD, 0);
			mv.visitVarInsn(Opcodes.ALOAD, 1);
			mv.visitFieldInsn(Opcodes.PUTFIELD, "org/codehaus/groovy/reflection/GroovyClassValueJava7", "computeValue", "Lorg/codehaus/groovy/reflection/GroovyClassValue$ComputeValue;");
			mv.visitInsn(Opcodes.RETURN);
			mv.visitMaxs(2, 2);
			mv.visitEnd();
		}
		{
			mv = cw.visitMethod(Opcodes.ACC_PROTECTED, "computeValue", "(Ljava/lang/Class;)Ljava/lang/Object;", "(Ljava/lang/Class<*>;)TT;", null);
			mv.visitCode();
			mv.visitVarInsn(Opcodes.ALOAD, 0);
			mv.visitFieldInsn(Opcodes.GETFIELD, "org/codehaus/groovy/reflection/GroovyClassValueJava7", "computeValue", "Lorg/codehaus/groovy/reflection/GroovyClassValue$ComputeValue;");
			mv.visitVarInsn(Opcodes.ALOAD, 1);
			mv.visitMethodInsn(Opcodes.INVOKEINTERFACE, "org/codehaus/groovy/reflection/GroovyClassValue$ComputeValue", "computeValue", "(Ljava/lang/Class;)Ljava/lang/Object;", true);
			mv.visitInsn(Opcodes.ARETURN);
			mv.visitMaxs(2, 2);
			mv.visitEnd();
		}
		cw.visitEnd();
		final byte[] bytes = cw.toByteArray();
		return new ClassLoader(GroovyClassValueFactory.class.getClassLoader()){
			public Class getGroovyClassValueJava7(){
				Class clazz = defineClass("org.codehaus.groovy.reflection.GroovyClassValueJava7", bytes, new Integer(0), new Integer(bytes.length));
				resolveClass(clazz);
				return clazz;
			}
		}.getGroovyClassValueJava7();
	}

	public static <T> GroovyClassValue<T> createGroovyClassValue(ComputeValue<T> computeValue){
		try {
			return (GroovyClassValue<T>) groovyClassValueConstructor.newInstance(computeValue);
		} catch (Exception e) {
			throw new RuntimeException(e); // this should never happen, but if it does, let it propagate and be fatal
		}
	}
}
