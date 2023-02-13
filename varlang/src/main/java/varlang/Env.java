package varlang;

/**
 * Representation of an environment, which maps variables to values.
 * 
 * @author hridesh
 *
 */
public interface Env {
	Value get (String search_var);

	void setEncryptedValue(String name, Value value);
	Value getEncryptedValue(String name);

	boolean isEmpty();

	@SuppressWarnings("serial")
	static public class LookupException extends RuntimeException {
		LookupException(String message){
			super(message);
		}
	}
	
	static public class EmptyEnv implements Env {
		public Value get (String search_var) {
			throw new LookupException("No binding found for name: " + search_var);
		}

		@Override
		public void setEncryptedValue(String name, Value value) {}

		@Override
		public Value getEncryptedValue(String name) {return null;}

		public boolean isEmpty() { return true; }
	}
	
	static public class ExtendEnv implements Env {
		private Env _saved_env; 
		private String _var; 
		private Value _val; 
		public ExtendEnv(Env saved_env, String var, Value val){
			_saved_env = saved_env;
			_var = var;
			_val = val;
		}
		public synchronized Value get (String search_var) {
			if (search_var.equals(_var))
				return _val;
			return _saved_env.get(search_var);
		}

		@Override
		public void setEncryptedValue(String name, Value value) {
			this._saved_env.setEncryptedValue(name, value);
		}

		@Override
		public Value getEncryptedValue(String name) {
			return this._saved_env.getEncryptedValue(name);
		}

		public boolean isEmpty() { return false; }
		public Env saved_env() { return _saved_env; }
		public String var() { return _var; }
		public Value val() { return _val; }
	}

	static public class GlobalEnv implements Env {
		private java.util.Hashtable<String, Value> map;
		private java.util.Hashtable<String, Value> encryptedMap;
		public GlobalEnv(){
			map = new java.util.Hashtable<String, Value>();
			encryptedMap = new java.util.Hashtable<String, Value>();
		}
		public synchronized Value get (String search_var) {
			if(map.containsKey(search_var))
				return map.get(search_var);
			throw new LookupException("No binding found for name: " + search_var);
		}

		@Override
		public void setEncryptedValue(String name, Value value) {
			this.encryptedMap.put(name, value);
		}

		@Override
		public Value getEncryptedValue(String name) {
			if(this.encryptedMap.containsKey(name))
				return this.encryptedMap.get(name);
			throw new LookupException("No binding found for name: " + name);
		}

		public synchronized void extend (String var, Value val) {
			map.put(var, val);
		}
		public boolean isEmpty() { return map.isEmpty(); }
	}

}
