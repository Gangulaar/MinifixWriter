

public class Transaction
{
	private String QuoteRequest;
    private String QuoteResponse;
    private String Execution;
    private String Order;
    private String InboundOrder;
    public String getRequest() 
    {
        return QuoteRequest;
    }
    public void setRequest(String QuoteRequest) 
    {
        this.QuoteRequest = QuoteRequest;
    }
    public String getQuote() 
    {
        return QuoteResponse;
    }
    public void setQuote(String QuoteResponse) 
    {
        this.QuoteResponse = QuoteResponse;
    }
    public String getExecution() 
    {
        return Execution;
    }
    public void setExecution(String Execution) 
    {
        this.Execution = Execution;
    }
    public String getTrade() 
    {
        return Order;
    }
    public void setTrade(String Order) 
    {
        this.Order = Order;
    }
    public String getInboundOrder() 
    {
        return InboundOrder;
    }
    public void setInboundOrder(String InboundOrder) 
    {
        this.InboundOrder = InboundOrder;
    } 
    public void displayAll()
    {
    	System.out.println("InboundOrder : " + this.InboundOrder);
    	System.out.println();
    	System.out.println("QuoteRequest : " + this.QuoteRequest);
		System.out.println("QuoteResponse : " + this.QuoteResponse);
		System.out.println("Order : " + this.Order);
		System.out.println("Execution : " + this.Execution);
		System.out.println("=============================================================================");
    }
}