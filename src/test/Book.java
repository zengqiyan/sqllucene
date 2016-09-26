package test;
public class Book{
		private long id; 
		private String bookname; 
		private String ename;
		private String type;
		private Double price;
		private Long date;
		public Book(long id,String bookname,String ename,String type,Double price,Long date){
			this.id=id; 
			this.bookname=bookname; 
			this.ename=ename;
			this.type=type;
			this.price=price;
			this.date=date;
		}
		public long getId() {
			return id;
		}
		public void setId(long id) {
			this.id = id;
		}
		public String getBookname() {
			return bookname;
		}
		public void setBookname(String bookname) {
			this.bookname = bookname;
		}
		public String getEname() {
			return ename;
		}
		public void setEname(String ename) {
			this.ename = ename;
		}
		public String getType() {
			return type;
		}
		public void setType(String type) {
			this.type = type;
		}
		public Double getPrice() {
			return price;
		}
		public void setPrice(Double price) {
			this.price = price;
		}
		public Long getDate() {
			return date;
		}
		public void setDate(Long date) {
			this.date = date;
		}
	
	 }