package my.lq.xlsimp.excel.counter;

import java.math.BigDecimal;

import org.jooq.Field;

public interface ICounter {
	public Field<Object> getField();
	public Object getNextValue();
}
