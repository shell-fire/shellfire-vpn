package de.shellfire.vpn.gui.helper;

import de.shellfire.vpn.Util.ExceptionThrowingReturningRunnable;

public abstract class ExceptionThrowingReturningRunnableImpl<T> implements ExceptionThrowingReturningRunnable<T> {

	private boolean isCancelled;

	@Override
	public abstract T run() throws Exception;

	@Override
	public boolean isCancellled() {
		return this.isCancelled;
	}

	@Override
	public void cancel() {
		this.isCancelled = true;
		
	}

}
