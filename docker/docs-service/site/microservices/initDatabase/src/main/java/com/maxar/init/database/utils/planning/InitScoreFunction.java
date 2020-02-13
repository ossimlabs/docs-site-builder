package com.maxar.init.database.utils.planning;

import java.util.List;

import com.radiantblue.analytics.core.Context;
import com.radiantblue.analytics.isr.core.component.accgen.IAccess;
import com.radiantblue.analytics.isr.core.component.schedule.score.IScore;
import com.radiantblue.analytics.isr.core.component.schedule.score.IScoreFunction;
import com.radiantblue.analytics.isr.core.op.IModeOp;

public class InitScoreFunction implements
		IScoreFunction
{
	private static IScore score = new IScore() {

		@Override
		public int compareTo(
				final IScore o ) {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public double value() {
			// TODO Auto-generated method stub
			return 1.0;
		}
	};

	@Override
	public IScore scoreAccess(
			final IAccess access ) {
		// TODO Auto-generated method stub
		return score;
	}

	@Override
	public IScore scoreOp(
			final IModeOp modeOp,
			final List<IModeOp> modeOps ) {
		return score;
	}

	@Override
	public IScore scoreOp(
			final IModeOp modeOp,
			final List<IModeOp> modeOps,
			final Context context ) {
		return score;
	}
}
