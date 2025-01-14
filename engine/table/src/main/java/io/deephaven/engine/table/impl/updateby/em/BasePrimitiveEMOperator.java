package io.deephaven.engine.table.impl.updateby.em;

import io.deephaven.api.updateby.BadDataBehavior;
import io.deephaven.api.updateby.OperationControl;
import io.deephaven.engine.rowset.RowSet;
import io.deephaven.engine.table.MatchPair;
import io.deephaven.engine.table.impl.locations.TableDataException;
import io.deephaven.engine.table.impl.updateby.UpdateByOperator;
import io.deephaven.engine.table.impl.updateby.internal.BaseDoubleUpdateByOperator;
import io.deephaven.engine.table.impl.util.RowRedirection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static io.deephaven.util.QueryConstants.NULL_LONG;

public abstract class BasePrimitiveEMOperator extends BaseDoubleUpdateByOperator {
    protected final OperationControl control;
    protected final double opAlpha;
    protected double opOneMinusAlpha;
    final EmFunction aggFunction;

    public interface EmFunction {
        double apply(double prevVal, double curVal, double alpha, double oneMinusAlpha);
    }

    public abstract class Context extends BaseDoubleUpdateByOperator.Context {
        double alpha;
        double oneMinusAlpha;
        long lastDt = NULL_LONG;
        long lastStamp = NULL_LONG;

        Context(final int chunkSize) {
            super(chunkSize);
        }

        @Override
        public void reset() {
            super.reset();
            lastStamp = NULL_LONG;
        }
    }

    /**
     * An operator that computes an EM output from an input column using an exponential decay function.
     *
     * @param pair the {@link MatchPair} that defines the input/output for this operation
     * @param affectingColumns the names of the columns that affect this ema
     * @param rowRedirection the row redirection to use for the EM operator output columns
     * @param control the control parameters for EM operator
     * @param timestampColumnName an optional timestamp column. If this is null, it will be assumed time is measured in
     *        integer ticks.
     * @param windowScaleUnits the smoothing window for the EM operator. If no {@code timestampColumnName} is provided,
     *        this is measured in ticks, otherwise it is measured in nanoseconds.
     */
    public BasePrimitiveEMOperator(@NotNull final MatchPair pair,
            @NotNull final String[] affectingColumns,
            @Nullable final RowRedirection rowRedirection,
            @NotNull final OperationControl control,
            @Nullable final String timestampColumnName,
            final long windowScaleUnits,
            @NotNull final EmFunction aggFunction) {
        super(pair, affectingColumns, rowRedirection, timestampColumnName, windowScaleUnits, 0, false);
        this.control = control;
        this.aggFunction = aggFunction;

        opAlpha = Math.exp(-1.0 / (double) windowScaleUnits);
        opOneMinusAlpha = 1 - opAlpha;

    }

    @Override
    public void initializeCumulative(@NotNull final UpdateByOperator.Context updateContext,
            final long firstUnmodifiedKey,
            final long firstUnmodifiedTimestamp,
            @NotNull final RowSet bucketRowSet) {
        super.initializeCumulative(updateContext, firstUnmodifiedKey, firstUnmodifiedTimestamp, bucketRowSet);

        final Context ctx = (Context) updateContext;
        // rely on the caller to validate this is a valid timestamp (or NULL_LONG when appropriate)
        ctx.lastStamp = firstUnmodifiedTimestamp;
    }

    void handleBadData(@NotNull final Context ctx,
            final boolean isNull,
            final boolean isNan) {
        boolean doReset = false;
        if (isNull) {
            if (control.onNullValueOrDefault() == BadDataBehavior.THROW) {
                throw new TableDataException("Encountered null value during Exponential Moving output processing");
            }
            doReset = control.onNullValueOrDefault() == BadDataBehavior.RESET;
        } else if (isNan) {
            if (control.onNanValueOrDefault() == BadDataBehavior.THROW) {
                throw new TableDataException("Encountered NaN value during Exponential Moving output processing");
            } else if (control.onNanValueOrDefault() == BadDataBehavior.POISON) {
                ctx.curVal = Double.NaN;
            } else {
                doReset = control.onNanValueOrDefault() == BadDataBehavior.RESET;
            }
        }

        if (doReset) {
            ctx.reset();
        }
    }
}
