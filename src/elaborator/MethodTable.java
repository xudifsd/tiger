package elaborator;

import java.util.Map;
import java.util.Set;

public class MethodTable {
	private java.util.Hashtable<String, ast.type.T> table;
	// codegen will need this infomation
	public java.util.Hashtable<String, ast.type.T> formals;
	public java.util.Hashtable<String, ast.type.T> locals;

	public MethodTable() {
		this.table = new java.util.Hashtable<String, ast.type.T>();
	}

	// Duplication is not allowed
	public void put(java.util.LinkedList<ast.dec.T> formals,
			java.util.LinkedList<ast.dec.T> locals) {

		this.formals = new java.util.Hashtable<String, ast.type.T>();
		this.locals = new java.util.Hashtable<String, ast.type.T>();

		for (ast.dec.T dec : formals) {
			ast.dec.Dec decc = (ast.dec.Dec) dec;
			if (this.table.get(decc.id) != null) {
				System.out.println("duplicated parameter: " + decc.id);
				System.exit(1);
			}
			this.table.put(decc.id, decc.type);
			this.formals.put(decc.id, decc.type);
		}

		for (ast.dec.T dec : locals) {
			ast.dec.Dec decc = (ast.dec.Dec) dec;
			if (this.table.get(decc.id) != null) {
				System.out.println("duplicated variable: " + decc.id);
				System.exit(1);
			}
			this.table.put(decc.id, decc.type);
			this.locals.put(decc.id, decc.type);
		}
	}

	// return null for non-existing keys
	public ast.type.T get(String id) {
		return this.table.get(id);
	}

	public void dump(String methodName) {
		Set<Map.Entry<String, ast.type.T>> set = table.entrySet();
		System.out.format("dump of methodT %s:\n", methodName);

		for (Map.Entry<String, ast.type.T> entry: set)
			System.out.format("\t%s %s\n", entry.getKey(), entry.getValue().toString());
	}

	@Override
	public String toString() {
		return this.table.toString();
	}
}
