package datainput;

/*
 * by dungkhmt@gmail.com
 * start date: 2017-03-15
 */
import java.util.ArrayList;

public class Staff {
	public String name;
	public ArrayList<Integer> maxShifts;
	public ArrayList<Integer> dayOff;
	public ArrayList<Integer> requestDayOn;
	public ArrayList<Integer> requestShiftOn;
	public ArrayList<Integer> requestOnWeight;

	public ArrayList<Integer> requestDayOff;
	public ArrayList<Integer> requestShiftOff;
	public ArrayList<Integer> requestOffWeight;
	public int maxWorkingTime;
	public int minWorkingTime;
	public int maxConsecutiveShifts;
	public int minConsecutiveShifts;
	public int minConsecutiveDayOFF;
	public int maxWeekends;

	public Staff(String info) {
		String[] s = info.split(",");
		name = s[0];
		maxWorkingTime = Integer.valueOf(s[2]);
		minWorkingTime = Integer.valueOf(s[3]);
		maxConsecutiveShifts = Integer.valueOf(s[4]);
		minConsecutiveShifts = Integer.valueOf(s[5]);
		minConsecutiveDayOFF = Integer.valueOf(s[6]);
		maxWeekends = Integer.valueOf(s[7]);

		maxShifts = new ArrayList<Integer>();
		dayOff = new ArrayList<Integer>();
		requestDayOff = new ArrayList<Integer>();
		requestShiftOff = new ArrayList<Integer>();
		requestOffWeight = new ArrayList<Integer>();
		requestDayOn = new ArrayList<Integer>();
		requestShiftOn = new ArrayList<Integer>();
		requestOnWeight = new ArrayList<Integer>();

		String[] tmp = s[1].split("\\|");
		for (int i = 0; i < tmp.length; i++) {
			maxShifts.add(Integer.valueOf((tmp[i].split("="))[1]));
		}
	}

	public void addListDayOff(String nextLine) {
		String[] data = nextLine.split(",");
		for (int i = 1; i < data.length; i++)
			dayOff.add(Integer.valueOf(data[i]));
	}

}
