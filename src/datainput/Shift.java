package datainput;

/*
 * by dungkhmt@gmail.com
 * start date: 2017-03-15
 */
import java.util.ArrayList;

public class Shift {
	public String name;
	public int length;// duration (in minutes) of the shift
	public ArrayList<String> shiftNameNotFollow;
	public int[] shiftNotFollow; // follow_ex[sh] is the list of forbidden following shifts of the current shift
	
	public Shift(String info) {
		shiftNameNotFollow = new ArrayList<>();
		String[] s = info.split(",");
		name = s[0];
		length = Integer.valueOf(s[1]);
		if (s.length == 3) {
			if (s[2].contains("|")) {
				String[] tmp = s[2].split("\\|");
				for (int i = 0; i < tmp.length; i++)
					if (tmp[i].length() > 0
							&& tmp[i].equalsIgnoreCase("|") == false) {
						shiftNameNotFollow.add(tmp[i]);
					}
			} else {
				shiftNameNotFollow.add(s[2]);
			}
		}
		shiftNotFollow = new int[shiftNameNotFollow.size()];
	}

}
