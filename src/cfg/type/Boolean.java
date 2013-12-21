package cfg.type;

public class Boolean extends T {
	public Boolean() {
	}

	@Override
	public String toString() {
		return "@boolean";
	}

	@Override
	public void accept(cfg.Visitor v) {
		v.visit(this);
	}
}