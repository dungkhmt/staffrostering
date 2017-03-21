package datainput;

/*
 * by dungkhmt@gmail.com
 * start date: 2017-03-15
 */
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;

import model.Constants;

public class SRInput {
	public int nbDays;
	public int nbShifts;
	public int nbStaffs;

	public ArrayList<Staff> listStaffs;
	public ArrayList<Shift> listShifts;
	public int[][] demand; // demand[d][shf] = number of nurses allocated in the
							// shift shf on day d

	private int shiftNameToIndex(String shiftName) {
		for (int i = 0; i < listShifts.size(); i++)
			if (listShifts.get(i).name.equalsIgnoreCase(shiftName))
				return i;
		return -1;
	}

	private String getNextLine(Scanner scanner) {
		String line = "";
		do {
			try {
				line = scanner.nextLine();
			} catch (Exception e) {
				e.printStackTrace();
				line = "";
				break;
			}
		} while (line.length() == 0 || line.charAt(0) == '#');
		return line;
	}

	private int nurseNameToIndex(String name) {
		int index = 0;
		for (int i = name.length() - 1; i >= 0; i--) {
			index += (name.charAt(i) - 'A' + 1)
					* Math.pow(26, name.length() - 1 - i);
		}
		index--;
		return index;
	}

	private void addDemand(String line) {
		String[] data = line.split(",");
		demand[Integer.valueOf(data[0])][shiftNameToIndex(data[1])] = Integer
				.valueOf(data[2]);
	}

	private void addRequestOn(int staffId, String line) {
		String[] data = line.split(",");
		listStaffs.get(staffId).requestDayOn.add(Integer.valueOf(data[1]));
		listStaffs.get(staffId).requestShiftOn.add(shiftNameToIndex(data[2]));
		listStaffs.get(staffId).requestOnWeight.add(Integer.valueOf(data[3]));
	}

	private void addRequestOff(int staffId, String line) {
		String[] data = line.split(",");
		listStaffs.get(staffId).requestDayOff.add(Integer.valueOf(data[1]));
		listStaffs.get(staffId).requestShiftOff.add(shiftNameToIndex(data[2]));
		listStaffs.get(staffId).requestOffWeight.add(Integer.valueOf(data[3]));
	}

	private void shiftFixFollow() {
		for (int k = 0; k < listShifts.size(); k++) {
			for (int k1 = 0; k1 < listShifts.get(k).shiftNameNotFollow.size(); k1++)
				listShifts.get(k).shiftNotFollow[k1] = shiftNameToIndex(listShifts
						.get(k).shiftNameNotFollow.get(k1));
		}
	}

	public void loadDataInput(String filename) {
		listStaffs = new ArrayList<Staff>();
		listShifts = new ArrayList<Shift>();
		Scanner scanner;
		String line = "";
		try {
			scanner = new Scanner(new File(filename));
			line = getNextLine(scanner); // Skip heading
			nbDays = Integer.valueOf(getNextLine(scanner));
			System.out.println("nDay = " + nbDays);

			line = getNextLine(scanner); // Skip heading
			line = getNextLine(scanner);
			while (line.startsWith("SECTION") == false) {
				listShifts.add(new Shift(line));
				line = getNextLine(scanner);
			}
			shiftFixFollow();
			nbShifts = listShifts.size();

			/* Read section staff */
			line = getNextLine(scanner);
			while (line.startsWith("SECTION") == false) {
				listStaffs.add(new Staff(line));
				line = getNextLine(scanner);
			}
			nbStaffs = listStaffs.size();

			/* Read section day-off */
			line = getNextLine(scanner);
			int id = 0;
			while (line.startsWith("SECTION") == false) {
				listStaffs.get(id).addListDayOff(line);
				line = getNextLine(scanner);
				id++;
			}
			/* Read section shift-on-request */
			line = getNextLine(scanner);
			id = 0;
			while (line.startsWith("SECTION") == false) {
				addRequestOn(nurseNameToIndex(line.split(",")[0]), line);
				line = getNextLine(scanner);
			}
			/* Read section shift-off-request */
			line = getNextLine(scanner);
			while (line.startsWith("SECTION") == false) {
				addRequestOff(nurseNameToIndex(line.split(",")[0]),
						line);
				line = getNextLine(scanner);
			}
			/* Read section cover */
			demand = new int[nbDays][nbShifts];
			line = getNextLine(scanner);
			while (line.equalsIgnoreCase("") == false) {
				addDemand(line);
				line = getNextLine(scanner);
			}
			scanner.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	public String getShiftName(int sh){
		if(sh == Constants.SHIFT_OFF) return "OFF";
		else return listShifts.get(sh).name;
	}

	public static void main(String[] args){
	}
}
