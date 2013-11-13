package ast.exp;

public class ArraySelect extends T {
	public T array;
	public T index;
	public int lineno;

	public ArraySelect(T array, T index, int lineno) {
		this.array = array;
		this.index = index;
		this.lineno = lineno;
	}

	@Override
	public void accept(ast.Visitor v) {
		v.visit(this);
		return;
	}
}
