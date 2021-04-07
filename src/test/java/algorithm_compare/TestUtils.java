package algorithm_compare;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

public class TestUtils {
	public static final String LEVEL_0_SEPARATOR = ", ";
	public static final String LEVEL_1_SEPARATOR = " ";
	
	public static String[] level0Separation(String raw) {
		if (raw.equals("")) return new String[0];
		return raw.split(LEVEL_0_SEPARATOR);
	}
	
	public static Iterator<List<Integer>> level1Separation(String raw) {
		return level1Separation(level0Separation(raw));
	}
	
	public static Iterator<List<Integer>> level1Separation(String[] split) {
		return new Iterator<List<Integer>>() {
			private int i = 0;
			@Override
			public boolean hasNext() {
				return i < split.length;
			}

			@Override
			public List<Integer> next() {
				List<String> ret = Arrays.asList(split[i++].split(LEVEL_1_SEPARATOR));
				
				return ret.stream().map((s) -> Integer.parseInt(s)).collect(Collectors.toList());
			}
		};
	}
}
