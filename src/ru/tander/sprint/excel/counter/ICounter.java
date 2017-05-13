package ru.tander.sprint.excel.counter;

import java.math.BigDecimal;

import org.jooq.Field;

public interface ICounter {
	public Field<BigDecimal> getField();
	public Object getNextValue();
}
