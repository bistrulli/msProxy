package mnt;

public class Event {
	private Long start=null;
	private Long end=null;
	
	public Event(long st,long end) {
		this.start=st;
		this.end=end;
	}

	public Long getStart() {
		return start;
	}

	public void setStart(Long start) {
		this.start = start;
	}

	public Long getEnd() {
		return end;
	}

	public void setEnd(Long end) {
		this.end = end;
	}
}
