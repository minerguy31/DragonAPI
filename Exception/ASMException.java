/*******************************************************************************
 * @author Reika Kalseki
 * 
 * Copyright 2015
 * 
 * All rights reserved.
 * Distribution of the software in any form is only allowed with
 * explicit, prior permission from the owner.
 ******************************************************************************/
package Reika.DragonAPI.Exception;

import net.minecraftforge.classloading.FMLForgePlugin;

import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;

public abstract class ASMException extends RuntimeException {

	public static final boolean DEV_ENV = !FMLForgePlugin.RUNTIME_DEOBF;


	protected final ClassNode node;

	protected ASMException(ClassNode cn) {
		node = cn;
	}

	@Override
	public String getMessage() {
		StringBuilder sb = new StringBuilder();
		sb.append("Error ASMing "+node.name+":\n");
		return sb.toString();
	}

	private abstract static class NoSuchMemberASMException extends ASMException {

		protected final String label;

		private NoSuchMemberASMException(ClassNode cn, String name) {
			super(cn);
			label = name;
		}

		@Override
		public final String getMessage() {
			StringBuilder sb = new StringBuilder();
			sb.append(super.getMessage());
			sb.append(this.getTitle());
			sb.append(" not found in class ");
			sb.append(node.name);
			sb.append(".\n");
			sb.append("This is a critical ASM error and the class transformer operation cannot proceed.");
			sb.append(" If you are the developer of this mod, check for proper use of SRG/deobf names and/or sideonly elements.");
			sb.append(" If not, report it to the developer.");
			sb.append("\n\nAdditional information:\n");
			sb.append(this.getAdditionalInformation());
			return sb.toString();
		}

		protected abstract String getAdditionalInformation();
		protected abstract String getTitle();

		public final boolean isVanillaClass() {
			return ASMException.isVanillaClass(node); //need the direct class reference or compiler has a seizure <_<
		}

	}

	@Override
	public final String toString() {
		return super.toString();
	}

	private static final boolean isVanillaClass(ClassNode node) {
		return !node.name.startsWith("net.minecraftforge") && !node.name.startsWith("cpw");
	}

	public static final class NoSuchASMMethodException extends NoSuchMemberASMException {

		private final String signature;

		public NoSuchASMMethodException(ClassNode cn, String name, String sig) {
			super(cn, name);
			signature = sig;
		}

		@Override
		protected String getTitle() {
			return "Method "+label+" "+signature;
		}

		@Override
		protected String getAdditionalInformation() {
			StringBuilder sb = new StringBuilder();
			if (DEV_ENV && label.startsWith("func_")) {
				sb.append("Use of SRG name in dev environment. This may be an error.\n");
			}
			else if (!DEV_ENV && !label.startsWith("func_") && this.isVanillaClass()) {
				sb.append("Use of non-SRG name in compiled game on vanilla code. This is very likely an error.\n");
			}
			sb.append("Identified methods:\n");
			for (MethodNode m : node.methods) {
				String tag = m.name.equals(label) ? " * Name match" : "";
				sb.append("\t"+m.name+" "+m.desc+tag+"\n");
			}
			return sb.toString();
		}

	}

	public static final class NoSuchASMFieldException extends NoSuchMemberASMException {

		public NoSuchASMFieldException(ClassNode cn, String name) {
			super(cn, name);
		}

		@Override
		protected String getTitle() {
			return "Field "+label;
		}

		@Override
		protected String getAdditionalInformation() {
			StringBuilder sb = new StringBuilder();
			if (DEV_ENV && label.startsWith("field_")) {
				sb.append("Use of SRG name in dev environment. This may be an error.\n");
			}
			else if (!DEV_ENV && !label.startsWith("field_") && this.isVanillaClass()) {
				sb.append("Use of non-SRG name in compiled game on vanilla code. This is very likely an error.\n");
			}
			sb.append("Identified fields:\n");
			for (FieldNode f : node.fields) {
				String tag = f.name.equals(label) ? " * Name match" : "";
				sb.append("\t"+f.name+" "+f.desc+tag+"\n");
			}
			return sb.toString();
		}

	}

}
