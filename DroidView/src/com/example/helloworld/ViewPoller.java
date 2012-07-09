package com.example.helloworld;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.example.helloworld.TouchPaint.PaintView;

public class ViewPoller extends Thread{

	TouchPaint mTouchPaint;
	public long pollerCurrentTime;
	
	  private List _listeners = new ArrayList();
	  
	  public synchronized void addEventListener(MyEventClassListener listener)	{
	    _listeners.add(listener);
	  }
	  public synchronized void removeEventListener(MyEventClassListener listener)	{
	    _listeners.remove(listener);
	  }

	  // call this method whenever you want to notify
	  //the event listeners of the particular event
	  private synchronized void fireEvent()	{
	    MyEventClass event = new MyEventClass(this);
	    Iterator i = _listeners.iterator();
	    while(i.hasNext())	{
	      ((MyEventClassListener) i.next()).handleMyEventClassEvent(event);
	    }
	  }
	
	public ViewPoller (TouchPaint touchPaint)
	{
		_listeners.add(touchPaint);
		mTouchPaint = touchPaint;
	}
	
	public void run()
	{
		while(true)
		{
			pollerCurrentTime = System.currentTimeMillis();
			if (this.mTouchPaint.mView.lastpointtime != 0 && (pollerCurrentTime-this.mTouchPaint.mView.lastpointtime)>1000)
			{	
				this.fireEvent();
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
