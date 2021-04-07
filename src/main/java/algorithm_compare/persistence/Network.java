package algorithm_compare.persistence;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class Network implements Serializable {

	private String name;
	private transient String path;
	private Map<String, Long> recordedTimes;
	
	private static final long serialVersionUID = 1L;
	
	public Network() {
		
	}
	
	public Network(String name) {
		this.name = name;
		recordedTimes = new HashMap<String, Long>();
	}
	
	public void setPath(String path) {
		this.path = path;
	}
}
