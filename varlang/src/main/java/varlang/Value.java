package varlang;

public interface Value {
	public String toString();
	public boolean isEncrypted();
	public void setEncrypted(boolean en);
	static class NumVal implements Value {
		private double _val;
		private boolean _encrypted;
	    public NumVal(double v) { _val = v; } 
	    public double v() { return _val; }
		public void setEncrypted(boolean encrypted){
			_encrypted = encrypted;
		}
		public boolean isEncrypted() {
			return _encrypted;
		}
	    public String toString() {
	    	int tmp = (int) _val;
	    	if(tmp == _val) return "" + tmp;
	    	return "" + _val; 
	    }
	}
	static class UnitVal implements Value {
		public static final UnitVal v = new UnitVal();
		public String toString() { return ""; }

		@Override
		public boolean isEncrypted() {
			return false;
		}

		@Override
		public void setEncrypted(boolean en) {

		}
	}
	static class DynamicError implements Value {
		private String message = "Unknown dynamic error.";
		public DynamicError(String message) { this.message = message; }
		public String toString() { return "" + message; }
		@Override
		public boolean isEncrypted() {
			return false;
		}
		@Override
		public void setEncrypted(boolean en) {

		}
	}
}
