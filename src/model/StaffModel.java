package model;

/*
 * by dungkhmt@gmail.com
 * start date: 2017-03-16
 */
import java.util.ArrayList;

import datainput.SRInput;
import datainput.Shift;
import datainput.Staff;

public class StaffModel {
	
	private StaffRosteringModel globalModel;
	
	private SRInput input;
	private ArrayList<Shift> shifts;
	private Staff staff;// input structure of staff
	
	private int[] x_shift;// decision variable x_shift[d] is the shift (0..input.nShift-1) on day d
	
	
	public StaffRosteringModel getGlobalModel(){
		return globalModel;
	}
	public StaffModel(SRInput I, int idx, StaffRosteringModel globalModel){
		this.input = I;
		staff = I.listStaffs.get(idx);
		this.globalModel = globalModel;
		x_shift = new int[I.nbDays];
	}
	public int getScheduledShift(int d){
		return x_shift[d];
	}
	public SRInput getSRInput(){
		return input;
	}
	public Staff getStaff(){
		return staff;
	}
	public String name(){
		return "StaffModel";
	}
	public void setSchedule(int[] scheduleShift){
		for(int d = 0; d < scheduleShift.length; d++){
			//System.out.println(name() + "::setSchedule, scheduleShift[" + d + "] = " + scheduleShift[d]);
			x_shift[d] = scheduleShift[d];
		}
	}
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
