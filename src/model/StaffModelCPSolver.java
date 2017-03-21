package model;

/*
 * by dungkhmt@gmail.com
 * start date: 2017-03-16
 */


import java.util.ArrayList;

import datainput.SRInput;

public class StaffModelCPSolver {
	protected StaffModel model;
	protected int nbShifts;
	protected int nbDays;
	protected int SHIFT_OFF;// = -1
	protected int min_consecutive;// minimum consecutive shifts of each type
	protected int max_consecutive;// maximum consecutive shifts of each type
	protected int min_total_minutes;// minimum working minutes of the staff in the
									// schedule
	protected int max_total_minutes;// maximum working minutes of the staff in the
									// schedule
	protected int[][] shiftNotFollow; // shiftNotFollow[sh] is the list of shifts that
								// cannot follow shift sh
	protected int[] shift_duration;// shift_duration[sh] is the duration (in
									// minutes) of the shift sh
	// decision variables
	protected int[] x_shift;// x_shift[d] is the shift on day d (-1: SHIFT_OFF,
							// other values 0,..., nbShifts-1)
	protected int[] best_x_shift;// store best solution
	// incremental information
	protected int working_minutes;

	// search params
	protected boolean foundSolution;
	// data structure for backtrack search
	protected int start;
	protected int end;
	protected int[] stored_x;

	public StaffModelCPSolver(StaffModel model) {
		this.model = model;
		SHIFT_OFF = Constants.SHIFT_OFF;
		nbShifts = model.getSRInput().nbShifts;
		nbDays = model.getSRInput().nbDays;
		min_consecutive = model.getStaff().minConsecutiveShifts;
		max_consecutive = model.getStaff().maxConsecutiveShifts;
		SRInput I = model.getSRInput();
		min_total_minutes = model.getStaff().minWorkingTime;
		max_total_minutes = model.getStaff().maxWorkingTime;
		shiftNotFollow = new int[nbShifts][];
		for (int sh = 0; sh < nbShifts; sh++)
			shiftNotFollow[sh] = I.listShifts.get(sh).shiftNotFollow;

		shift_duration = new int[nbShifts];
		for (int sh = 0; sh < I.listShifts.size(); sh++) {
			shift_duration[sh] = I.listShifts.get(sh).length;
		}
		
		stored_x = new int[nbDays];
	}

	protected boolean checkfollowExclusive(int s1, int s2){
		// return false if s2 is in the forbidden shift list of s1
		// return true if shift s2 can be a successor of shift s1
		if(s1 == Constants.SHIFT_OFF || s2 == Constants.SHIFT_OFF) return true;
		for (int i = 0; i < shiftNotFollow[s1].length; i++)
			if (shiftNotFollow[s1][i] == s2) return false;
		return true;
		
	}
	protected boolean check(int sh, int k) {
		// return true if shift sh can be assigned to x[d] without violating
		// constraints
		
		//System.out.println(name() + "::check(" + sh + "," + k + "), partial solution = " + partialSolution(k-1));
		//for(int d = 0; d < nbDays; d++) System.out.print(stored_x[d] + " "); System.out.println();
		
		if (k == 0)
			return true;

		// count last consecutive identical shifts
		int count = 1;
		int j = k - 1;
		while (j > 0) {
			if (x_shift[j] != x_shift[j - 1])
				break;
			j--;
			count++;
		}

		if (x_shift[k - 1] == sh) {
			if (count >= max_consecutive)
				return false;
		} else {
			// check following shift in relation with predecessor shift
			// x_shift[k-1]

			int psh = x_shift[k - 1];// predecessor shift

			if (psh != SHIFT_OFF)
				//for (int i = 0; i < shiftNotFollow[psh].length; i++)
				//	if (shiftNotFollow[psh][i] == sh)
				if(!checkfollowExclusive(psh, sh))
						return false;// sh belong to the forbidden following
										// shifts of psh

			if (count < min_consecutive)
				return false;
		}

		// check amount of hours
		if (working_minutes > max_total_minutes)
			return false;

		if(k == nbDays-1) return true;
		
		int countSucc = 0;// number of identical shift assigned from position k+1
		if(stored_x[k+1] != Constants.NOT_ASSIGNED){
			int shs = stored_x[k+1];// successor shift
			countSucc = 1;
			j = k+2;
			while(j < nbDays){
				if(stored_x[j] == Constants.NOT_ASSIGNED) break;
				if(stored_x[j] != shs) break;
				j++;
				countSucc++;
			}
			
			if(sh != shs){
				if(!checkfollowExclusive(sh, shs)) return false;
			}else{
				int c = countSucc += 1;
				if(k >= 1)if(sh == x_shift[k-1]) c += count;// add prefix to the consecutive identical shifts
				if(c < min_consecutive || c > max_consecutive) return false;
			}
		}
		
		return true;
	}

	private void TRY(int k) {
		if (foundSolution)
			return;
		for (int sh = -1; sh < nbShifts; sh++) {
			if (check(sh, k)) {
				x_shift[k] = sh;
				if (sh != -1)// SHIFT_OF
					working_minutes += shift_duration[sh];
				if (k == nbDays - 1) {
					if (working_minutes >= min_total_minutes) {
						System.arraycopy(x_shift, 0, best_x_shift, 0,
								x_shift.length);
						foundSolution = true;
						//System.out.println(name() + "::TRY, found solution");
					}
				} else {
					TRY(k + 1);
				}
				if (sh != -1)// SHIFT_OF
					working_minutes -= shift_duration[sh];
			}else{
				//System.out.println(name() + "::TRY(" + k + ") failed --> backtrack");
			}
		}
	}

	public String name() {
		return "StaffModelCPSolver";
	}

	public void solve() {
		x_shift = new int[nbDays];
		best_x_shift = new int[nbDays];
		for(int d = 0; d < nbDays; d++)
			stored_x[d] = Constants.NOT_ASSIGNED;
		
		TRY(0);
		model.setSchedule(best_x_shift);
	}
	public String partialSolution(int k){
		String s = "";
		for(int i = 0; i <= k; i++)
			s += x_shift[i] + " ";
		return s;
	}
	public void printSolution() {
		for (int i = 0; i < best_x_shift.length; i++)
			System.out.print(best_x_shift[i] + " ");// + model.getScheduledShift(i));
		System.out.println();
	}

	public static void main(String[] args) {
		
	}
}
