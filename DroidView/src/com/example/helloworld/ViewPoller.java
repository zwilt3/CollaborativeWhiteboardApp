package com.example.helloworld;

import com.example.helloworld.TouchPaint.PaintView;

public class ViewPoller extends Thread{

	PaintView mPaintView;
	public long pollerCurrentTime;
	
	public ViewPoller (PaintView c)
	{
		mPaintView = c;
	}
	
	public void run()
	{
		while(true)
		{
			pollerCurrentTime = System.currentTimeMillis();
			if (this.mPaintView.lastpointtime != 0 && (pollerCurrentTime-this.mPaintView.lastpointtime)>1000)
			{
				this.mPaintView.pollerForceEnd();
			}
			try
			{
				Thread.sleep(1500);
			}
			catch(Exception e)
			{
			}
		}
	}
	
	
}
