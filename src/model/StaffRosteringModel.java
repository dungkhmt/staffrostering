package model;

/*
 * by dungkhmt@gmail.com
 * start date: 2017-03-17
 */

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Random;

import datainput.SRInput;

public class StaffRosteringModel {
	private SRInput input;
	private StaffModel[] staffModels;

	private int[][] supply;// supply[d][sh] is the number of staffs scheduled in
							// shift sh on day d
	private int evaluation;

	public StaffRosteringModel(SRInput input) {
		this.input = input;
		staffModels = new StaffModel[input.listStaffs.size()];
		for (int i = 0; i < input.listStaffs.size(); i++) {
			staffModels[i] = new StaffModel(input, i, this);
		}
		supply = new int[input.nbDays][input.listShifts.size()];
	}

	public int[][] getSupply() {
		return supply;
	}

	public StaffModel[] getStaffModels() {
		return staffModels;
	}

	public void computeSupply() {
		for (int d = 0; d < input.nbDays; d++)
			for (int sh = 0; sh < input.listShifts.size(); sh++)
				supply[d][sh] = 0;

		for (int d = 0; d < input.nbDays; d++) {
			for (int s = 0; s < input.listStaffs.size(); s++) {
				int sh = staffModels[s].getScheduledShift(d);
				if (sh >= 0) {
					supply[d][sh]++;
					//System.out.println(name() +
					// "::computeSupply, shift of staff " + s + " on day " + d +
					// " is " + sh +
					// ", supply[" + d + "," + sh + "] = " + supply[d][sh]);
				}
			}
		}
	}
	public int evaluation(int startDay, int endDay){
		int eval = 0;
		for (int d = startDay; d <= endDay; d++) {
			for (int sh = 0; sh < input.nbShifts; sh++) {
				eval += violations(supply[d][sh], input.demand[d][sh]);
				
				//if (supply[d][sh] > input.demand[d][sh])
				//	eval += (-input.demand[d][sh] + supply[d][sh])*1;
				//else if (supply[d][sh] < input.demand[d][sh])
				//	eval += (input.demand[d][sh] - supply[d][sh])*100;
			}
		}
		return eval;
	}
	public int evaluation() {
		evaluation = 0;
		for (int d = 0; d < input.nbDays; d++) {
			for (int sh = 0; sh < input.nbShifts; sh++) {
				evaluation += violations(supply[d][sh],input.demand[d][sh]);
				
				//if (supply[d][sh] > input.demand[d][sh])
				//	evaluation += (-input.demand[d][sh] + supply[d][sh])*1;
				//else if (supply[d][sh] < input.demand[d][sh])
				//	evaluation += (input.demand[d][sh] - supply[d][sh])*100;
			}
		}
		return evaluation;
	}

	public String name() {
		return "StaffRosteringModel";
	}

	public void printHTML(String fn) {
		try {
			PrintWriter out = new PrintWriter(fn);
			out.println("<table border = 1>");
			for (int i = 0; i < input.listStaffs.size(); i++) {
				out.println("<tr>");
				for (int d = 0; d < input.nbDays; d++) {
					out.println("<td height = 20 width = 30>");
					int sh = staffModels[i].getScheduledShift(d);
					out.println(input.getShiftName(sh));
					out.println("</td>");
				}
				out.println("</tr>");
			}
			out.println("</table>");
			out.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	public int violations(int supply, int demand){
		if(supply < demand) return (demand - supply)*100;
		else if(supply > demand) return (supply - demand)*1;
		return 0;
	}
	public ArrayList<StaffPeriod> findMostViolatingStaffDays(int len){
		// return staff-day that contribute most to the violations
		ArrayList<StaffPeriod> L = new ArrayList<StaffPeriod>();
		int most_violations = 0;
		int selected_day = -1;
		int selected_shift = -1;
		for(int d = 0; d < input.nbDays; d++){
			for(int sh = 0; sh < input.nbShifts; sh++){
				int v = violations(supply[d][sh],input.demand[d][sh]);
				if(v > most_violations){
					most_violations = v;
					selected_day = d;
					selected_shift = sh;
				}
			}
		}
		System.out.println(name() + "::findMostViolatingStaffDay, most_violations = " + most_violations + ", selected_day = " + selected_day + ", selected shift = " + selected_shift);
		for(int i = 0; i < input.listStaffs.size(); i++){
			for(int start = 0; start <= input.nbDays-len; start++){
				boolean ok = false;
				for(int d = start; d <= start+len-1; d++){
					int sh = getStaffModels()[i].getScheduledShift(d);
					if(d == selected_day && sh != selected_shift){
						ok = true; break;
					}
				}
				if(ok)
					L.add(new StaffPeriod(i,start,start+len-1));
			}
		}
		return L;
		
	}
	public int violations(int stf, int startDay, int endDay){
		// return the contributed penalty of the period (startDay...endDay) of the schedule of staff stf
		int eval = 0;
		for(int d  = startDay; d <= endDay; d++){
			int sh = staffModels[stf].getScheduledShift(d);
			if(sh == Constants.SHIFT_OFF){
				for(int shift = 0; shift < input.nbShifts; shift++){
					eval += violations(supply[d][shift], input.demand[d][shift]);
					
				}
			}else{
				if(supply[d][sh] > input.demand[d][sh]) eval += violations(supply[d][sh],input.demand[d][sh]);
				for(int shift = 0; shift < input.nbShifts; shift++){
					if(shift != sh && supply[d][shift] < input.demand[d][shift])
						eval += violations(supply[d][shift],input.demand[d][shift]);
				}
			}
			
		}
		return eval;
	}
	public StaffPeriod findMostViolatingStaffPeriod(int len){
		StaffPeriod selectedStaffPeriod = null;
		int maxViolations = 0;
		for(int staff = 0; staff < input.listStaffs.size(); staff++){
			for(int startDay = 0; startDay <= input.nbDays - len; startDay++){
				int endDay = startDay + len - 1;
				int eval = violations(staff, startDay, endDay);
				if(eval > maxViolations){
					maxViolations = eval;
					selectedStaffPeriod = new StaffPeriod(staff, startDay, endDay);
					//System.out.println(name() + "findMostViolatings staf-period, maxViolations = " + maxViolations + ", with " + staff + "," + startDay + "," + endDay);
					
				}
			}
		}
		return selectedStaffPeriod;
	}
	public void lns() {
		StaffModelCPSolverOpt[] modelOpt = new StaffModelCPSolverOpt[input.listStaffs
				.size()];
		for (int i = 0; i < input.listStaffs.size(); i++) {
			modelOpt[i] = new StaffModelCPSolverOpt(getStaffModels()[i]);
		}

		for (int i = 0; i < input.listStaffs.size(); i++) {
			StaffModel m = getStaffModels()[i];// new StaffModel(I, i, model);
			// StaffModelCPSolver s = new StaffModelCPSolver(m);
			modelOpt[i].solve();
			//modelOpt[i].printSolution();
			//System.exit(-1);
		}
		computeSupply();
		
		print();
		System.out.println(name() + "::lns, init evaluation = " + evaluation());
		//if(true) return;
		
		Random R = new Random();
		while (true) {
			
			
			//int staff = R.nextInt(modelOpt.length);
			//System.out.println(name() + "::lns, selected staff = " + staff);
			StaffPeriod staffPeriod = findMostViolatingStaffPeriod(14);
			System.out.println(name() + "::lns, selected (" + staffPeriod.staff + "," + staffPeriod.startDay + "," + staffPeriod.endDay + ")");
			
			modelOpt[staffPeriod.staff].solveOpt(staffPeriod.startDay, staffPeriod.endDay);
			if (!modelOpt[staffPeriod.staff].foundSolution) {
				
				boolean improvement2 = false;
				ArrayList<StaffPeriod> L = findMostViolatingStaffDays(14);
				System.out.println(name() + "::lns, improvement2, L.sz = " + L.size());
				for(int j = 0; j < L.size(); j++){
					StaffPeriod sp = L.get(j);
					System.out.println(name() + "::lns, improvement2, process " + j + "/" + L.size() + ", selected (" + sp.staff + "," + sp.startDay + "," + sp.endDay + ")");
					modelOpt[sp.staff].solveOpt(sp.startDay, sp.endDay);
					if(modelOpt[sp.staff].foundSolution){
						improvement2 = true;
						System.out.println(name() + "::lns, improvement2 FOUND!!!!!!");
						computeSupply();
						//break;
					}
				}
				if(improvement2){
					
				}else{
					System.out.println(name()
						+ "::lns, no improvement found --> break");
					break;
				}
			}else{
				computeSupply();
			}
			//print();
			//break;
		}
	}

	public void print() {
		for (int i = 0; i < input.listStaffs.size(); i++) {
			for (int d = 0; d < input.nbDays; d++) {
				int sh = staffModels[i].getScheduledShift(d);
				System.out.print(sh + " ");
			}
			System.out.println();
		}
		for (int sh = 0; sh < input.nbShifts; sh++) {
			System.out.print("shift " + sh + " : ");
			for (int d = 0; d < input.nbDays; d++) {
				System.out.print("[" + supply[d][sh] + ", " + input.demand[d][sh] + "] ");
			}
			System.out.println();
		}
		
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		SRInput I = new SRInput();
		I.loadDataInput("data/txt/Instance10.txt");
		StaffRosteringModel model = new StaffRosteringModel(I);
		model.lns();
		System.out.println("------------------RESULT------------------");
		model.print();
		model.printHTML("schedule.html");
	}

}
