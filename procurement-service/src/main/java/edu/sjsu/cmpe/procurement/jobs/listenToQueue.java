package edu.sjsu.cmpe.procurement.jobs;

import javax.jms.JMSException;

import edu.sjsu.cmpe.procurement.ProcurementService;
import edu.sjsu.cmpe.procurement.annotations.Every;

@Every("5mn")
public class listenToQueue extends Job {
	@Override
	public void doJob() {
		try{
	    ProcurementService.retrieveFromtheQueue();
		}
		catch(JMSException e)
		{
			System.out.println("JMS error throw while reading from the queue " + e.getMessage());
		}
	  }
}
