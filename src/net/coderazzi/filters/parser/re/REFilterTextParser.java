package net.coderazzi.filters.parser.re;

import net.coderazzi.filters.artifacts.RowFilter;
import net.coderazzi.filters.parser.FilterTextParsingException;
import net.coderazzi.filters.parser.IFilterTextParser;
import net.coderazzi.filters.parser.ITypeBuilder;
import net.coderazzi.filters.parser.IdentifierInfo;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * <p>
 * Specific implementation of the
 * {@link net.coderazzi.filters.parser.IFilterTextParser} interface, simply
 * providing regular expressions.
 * </p>
 * 
 * <p>
 * It does not define any grammar, it depends on the java implementation of
 * regular expressions.
 * </p>
 * 
 * <p>
 * It does not accept the usage of identifiers in the expression: the whole
 * expression is evaluated as a java regular expression.
 * </p>
 * 
 * @author Luis M Pena - sen@coderazzi.net
 */
public class REFilterTextParser extends RowFilter implements IFilterTextParser {

	private boolean ignoreCase; //this flag implies that we cannot use RowFilter.regexFilter
	private Pattern pattern;
	private int filterPosition;
	List<IdentifierInfo> validIdentifiers;

	@Override
	public boolean include(RowFilter.Entry entry) {
		if (pattern == null) {
			return true;
		}
		if (filterPosition != IFilterTextParser.NO_FILTER_POSITION) {
			return pattern.matcher(entry.getStringValue(filterPosition)).find();
		}
		for (IdentifierInfo info : validIdentifiers) {
			if (pattern.matcher(entry.getStringValue(info.filterPosition)).find()) {
				return true;
			}
		}

		return false;
	}

	/**
	 * @see IFilterTextParser#setIdentifiers(List)
	 */
	public void setIdentifiers(List<IdentifierInfo> validIdentifiers) {
		this.validIdentifiers = new ArrayList<IdentifierInfo>(validIdentifiers);
	}

	public RowFilter parseText(String expression, int defaultFilterPosition) throws FilterTextParsingException {
		filterPosition = defaultFilterPosition;
		try {
			if (ignoreCase) {
				pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE);
			} else {
				pattern = Pattern.compile(expression);
			}

			return this;
		} catch (PatternSyntaxException pse) {
			pattern = null;
			throw new FilterTextParsingException(pse.getLocalizedMessage(), pse.getIndex());
		}
	}

	public void setIgnoreCase(boolean ignore) {
		ignoreCase = ignore;
	}

	public void setComparator(Class<?> c, Comparator<?> cmp) {
	}

	public void setTypeBuilder(Class<?> c, ITypeBuilder parser) {
	}

}
