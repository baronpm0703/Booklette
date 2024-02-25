Create database booklette;

Use booklette;
CREATE TABLE Account (
	UID char(50) PRIMARY KEY,
	Fullname text,
	Phone text,
	Email text,
	Address text,
	ShippingAddress text
);

CREATE TABLE PurchaseHistory (
	OrderId int PRIMARY KEY,
	CustomerID char(50),
	CreationDate Date,
	Status char,
	TotalSum bigint,
	PaymentMethod text,
	FOREIGN KEY (CustomerID) REFERENCES Account (UID)
);

CREATE TABLE Book (
	BookID int PRIMARY KEY,
	Name text,
	Genre text,
	Author text,
	ReleaseDate Date,
	Image text
);

CREATE TABLE OrderItem (
	OrderID int,
	ItemID int,
	Quantity SMALLINT,
	TotalSum bigint,
	PRIMARY KEY (OrderID, ItemID),
	FOREIGN KEY (OrderID) REFERENCES PurchaseHistory (OrderId),
	FOREIGN KEY (ItemID) REFERENCES Book (BookID)
);

CREATE TABLE Review (
	BookID int,
	UID char(50),
	Score SMALLINT,
	Text text,
	Image text,
	PRIMARY KEY (BookID, UID),
	FOREIGN KEY (BookID) REFERENCES Book (BookID),
	FOREIGN KEY (UID) REFERENCES Account (UID)
);

CREATE TABLE PersonalStore (
	OwnerID char(50),
	ItemID int,
	Price bigint,
	Sold int,
	Remain int,
	Discount SMALLINT,
	PRIMARY KEY (OwnerID, ItemID),
	FOREIGN KEY (OwnerID) REFERENCES Account (UID),
	FOREIGN KEY (ItemID) REFERENCES Book (BookID)
);

CREATE TABLE ReturnExchange (
	CustomerID char(50),
	StoreID char(50),
	ItemID int,
	ReturnDate Date,
	Reason text,
	PRIMARY KEY (CustomerID, StoreID, ReturnDate),
	FOREIGN KEY (CustomerID) REFERENCES Account (UID),
	FOREIGN KEY (StoreID, ItemID) REFERENCES PersonalStore (OwnerID, ItemID)
);

CREATE TABLE Wishlist (
	UID char(50),
	ItemID int,
	PRIMARY KEY (UID, ItemID),
	FOREIGN KEY (UID) REFERENCES Account (UID),
	FOREIGN KEY (ItemID) REFERENCES Book (BookID)
);

CREATE TABLE Blacklist (
	UID char(50),
	SuspectID char(50),
	Reason text,
	SuspendDate Date,
	PRIMARY KEY (UID, SuspectID),
	FOREIGN KEY (UID) REFERENCES Account (UID),
	FOREIGN KEY (SuspectID) REFERENCES Account (UID)
);

CREATE TABLE Discount (
	DiscountID char(50),
	DiscountName text,
	DiscountType SMALLINT,
	DiscountTypeValue text,
	Percent SMALLINT,
	StartDate Date,
	EndDate Date,
	PRIMARY KEY (DiscountID, DiscountType)
)


Insert into booklette.Account Values
	('t4TBmyNG4hgrUWyM2eNFEYlAuFu2', 'Võ Chánh Tín', '0948363775','vctin21@clc.fitus.edu.vn', 'Chung cư quân khu gì đó', 'Chung quân khu gì đó' ),
    ('Wanlg9TtCkdvThrmCwheIeMlSHI3', 'Phan Thái Khang', '099471123','ptkhang21@clc.fitus.edu.vn', 'Quận 7, TP.HCM', 'Quận 5, TP.HCM' ),
    ('NSKxVRDePid6gmNndNGeEmAjsvS2', 'Nguyễn Lâm Hải', '0948361174','npmbao21@clc.fitus.edu.vn', '22 hẻm 182 Lý Thường Kiệt', '' ),
	('0HR9Q9Y1HJQvm59UoL4UiJQFXIJ2', 'Nguyễn Phú Minh Bảo', '0948361174','npmbao21@clc.fitus.edu.vn', '22 hẻm 182 Lý Thường Kiệt', '' );
    
Insert into  booklette.Book Values
	('01', 'Tuesday Mooney Talks to Ghosts', 'Tiểu Thuyết','Kate Racculia', '1996-12-01', ''),
    ('02', 'The Catcher in the eyes', 'Tiểu Thuyết','J. D. Salinger', '2003-02-01', ''),
    ('03', 'Great Expectations', 'Self-Help','Charles Dickens', '2008-11-11', ''),
    ('04', 'Tôi Thấy Hoa Vàng Trên Cỏ Xanh', 'Tiểu thuyết','Nguyễn Nhật Ánh', '2008-06-11-', ''),
    ('05', 'One-Punch Man', 'Truyện Tranh','Yusuke Murata, ONE', '2009-03-12', '');
    
    
    
    
    
	
    

