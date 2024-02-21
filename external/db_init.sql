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
	PRIMARY KEY (DiscountID, DiscountName, DiscountType, DiscountTypeValue)
)

