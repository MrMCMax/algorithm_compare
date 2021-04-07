package algorithm_compare.persistence;

import java.io.IOException;
import java.util.List;

public interface IPersistenceService {

	List<String> getAllNetworkNames() throws IOException;
	
	GraphData loadNetwork(String name) throws IOException;

}
