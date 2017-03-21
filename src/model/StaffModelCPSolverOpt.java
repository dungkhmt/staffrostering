package model;

/*
 * by dungkhmt@gmail.com
 * start date: 2017-03-17
 */
import java.util.Random;

import datainput.SRInput;

public class StaffModelCPSolverOpt extends StaffModelCPSolver {
	private SRInput input;

	protected int[][] supply;// supply[d][sh] is the number of staffs (exclusive
								// this) being scheduled in the shift sh on day
								// d

	protected int opt;

	private Random R = new Random();
	
	public StaffModelCPSolverOpt(StaffModel model) {
		super(model);
		supply = new int[nbDays][nbShifts];
		input = model.getSRInput();
	}

	protected void updateBest() {
		//System.out.print(name() + "::updateBest, shift = ");
		//for (int i = 0; i < x_shift.length; i++)
		//	System.out.print(x_shift[i] + " ");
		//System.out.println();
		
		//System.out.println(name() + "::updateBest, old supply: ");
		//printSupply();

		int f = 0;

		for (int d = start; d <= end; d++) {
			if (x_shift[d] != SHIFT_OFF) {
				supply[d][x_shift[d]] += 1;
			}
		}
		
		//System.out.println(name() + "::updateBest, updated supply: ");
		//printSupply();
		
		for (int d = 0; d < nbDays; d++) {
			for (int sh = 0; sh < nbShifts; sh++) {
				f += model.getGlobalModel().violations(supply[d][sh], input.demand[d][sh]);
				
				//if (supply[d][sh] < input.demand[d][sh])
				//	f += (input.demand[d][sh] - supply[d][sh])*100;
				//else if (supply[d][sh] > input.demand[d][sh])
				//	f += (-input.demand[d][sh] + supply[d][sh])*1;
			}
		}
		for (int d = start; d <= end; d++) {
			// recover
			if (x_shift[d] != SHIFT_OFF) {
				supply[d][x_shift[d]] -= 1;
			}

		}
		//for(int sh = 0; sh < input.nShifts; sh++){
		//	for(int d = 0; d < input.nDays; d++) System.out.print(supply[d][sh] + " ");
		//}
		//System.out.println();
		//model.getGlobalModel().print();
		
		
		//System.out
		//		.println(name() + "::updateBest, opt = " + opt + ", f = " + f);
		if (f < opt) {
			System.arraycopy(x_shift, 0, best_x_shift, 0, x_shift.length);
			opt = f;
			foundSolution = true;
			System.out.println(name() + "::updateBest, opt = " + opt);
		}

	}

	private void randomPermutation(int[] a){
		for(int k = 0; k < 5; k++){
			int j = R.nextInt(a.length-1);
			int i = R.nextInt(a.length-1);
			int tmp = a[i]; a[i] = a[j]; a[j] = tmp;
		}
	}
	private void TRY(int k) {
		if (foundSolution)
			return;
		int[] shifts = new int[nbShifts+1];
		for(int j = 0; j < shifts.length-1; j++) shifts[j] = j;
		shifts[shifts.length-1] = Constants.SHIFT_OFF;
		randomPermutation(shifts);
		
		//for (int sh = nbShifts-1; sh >= -1; sh--) {
		for(int j = 0; j < shifts.length; j++){
			int sh = shifts[j];
			if (check(sh, k)) {
				x_shift[k] = sh;
				if (sh != Constants.SHIFT_OFF)// SHIFT_OFF
					working_minutes += shift_duration[sh];
				if (k == end) {
					//System.out.println(name() + "::TRY(" + k + ") reach end, working_minutes = " + working_minutes + " min_total_minutes = " + min_total_minutes);
					if (working_minutes >= min_total_minutes) {
						updateBest();

					}
				} else {
					TRY(k + 1);
				}
				if (sh != -1)// SHIFT_OF
					working_minutes -= shift_duration[sh];
			}else{
				//System.out.println(name() + "::TRY, failed, backtrack");
			}
		}

	}

	public void printSupply() {
		for (int sh = 0; sh < input.nbShifts; sh++) {
			for (int d = 0; d < input.nbDays; d++) {
				System.out.print(supply[d][sh] + " ");

			}
			System.out.println();
		}

	}
	public void solveOpt() {
		solveOpt(0,input.nbDays-1);
	}
	public void solveOpt(int start, int end) {
		opt = model.getGlobalModel().evaluation();
		
		this.start = start;
		this.end = end;
		stored_x = new int[input.nbDays];
		for(int d = 0; d < input.nbDays; d++){
			stored_x[d] = model.getScheduledShift(d);
			x_shift[d] = stored_x[d];
		}
		for(int i = start; i <= end; i++){
			stored_x[i] = Constants.NOT_ASSIGNED;
			x_shift[i] = Constants.NOT_ASSIGNED;
		}
		working_minutes = 0;
		for(int d = 0; d < input.nbDays; d++)
			if(stored_x[d] != Constants.SHIFT_OFF && stored_x[d] != Constants.NOT_ASSIGNED) 
				working_minutes += shift_duration[stored_x[d]];
		
		int[][] s = model.getGlobalModel().getSupply();
		for(int d = 0; d < input.nbDays; d++)
			for(int sh = 0; sh < input.nbShifts; sh++)
				supply[d][sh] = s[d][sh];
		
		for (int d = start; d <= end; d++) {
			int sh = model.getScheduledShift(d);// x_shift[d];
			if (sh != SHIFT_OFF) {
				supply[d][sh] = s[d][sh] - 1;
			}
		}
		
		// System.out.println(name() + "::solveOpt, before TRY, supply = ");
		// printSupply();
		
		//opt = model.getGlobalModel().evaluation(start, end);
		
		foundSolution = false;
		
		TRY(start);
		
		if(foundSolution){
			model.setSchedule(best_x_shift);
		}
	}

	public String name(){
		return "StaffModelCPSolverOpt";
	}
}
