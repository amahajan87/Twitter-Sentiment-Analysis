

public class ResultCounter {
	public int negativeSentenceCount;
	public int positiveSentenceCount;
	public double negativeAggrigate;
	public double positiveAggrigate;
	public int negativeDocumentCount;
	public int positiveDocumentCount;
	
	public ResultCounter(int negativeCount, int positiveCount,
			double negativeAggrigate, double positiveAggrigate) {
		super();
		this.negativeSentenceCount = negativeCount;
		this.positiveSentenceCount = positiveCount;
		this.negativeAggrigate = negativeAggrigate;
		this.positiveAggrigate = positiveAggrigate;
	}

	public ResultCounter(int negativeSentenceCount, int positiveSentenceCount,
			double negativeAggrigate, double positiveAggrigate,
			int negativeDocumentCount, int positiveDocumentCount) {
		super();
		this.negativeSentenceCount = negativeSentenceCount;
		this.positiveSentenceCount = positiveSentenceCount;
		this.negativeAggrigate = negativeAggrigate;
		this.positiveAggrigate = positiveAggrigate;
		this.negativeDocumentCount = negativeDocumentCount;
		this.positiveDocumentCount = positiveDocumentCount;
	}

	@Override
	public String toString() {
		return "ResultCounter [negativeSentenceCount=" + negativeSentenceCount
				+ ", positiveSentenceCount=" + positiveSentenceCount
				+ ", negativeAggrigate=" + negativeAggrigate
				+ ", positiveAggrigate=" + positiveAggrigate
				+ ", negativeDocumentCount=" + negativeDocumentCount
				+ ", positiveDocumentCount=" + positiveDocumentCount + "]";
	}
	
}
