package com.example.fsm.examples.actions;

import com.example.fsm.ActionDelegate;
import java.util.Date;

class StopTimerAction extends ActionDelegate {


	public void perform() {
		System.out.println(this.getClass().toString()+":"+(new Date()));
	}

}