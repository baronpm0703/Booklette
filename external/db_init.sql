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
	(1, 'Tuesday Mooney Talks to Ghosts', 'Novel','Kate Racculia', '1996-12-01', 'https://firebasestorage.googleapis.com/v0/b/book-store-3ed32.appspot.com/o/Books%2FTuesday.jpg?alt=media&token=6a578daf-af25-434e-9952-ffa673a37d62'),
    (2, 'The Catcher in the eyes', 'Novel','J. D. Salinger', '2003-02-01', 'https://firebasestorage.googleapis.com/v0/b/book-store-3ed32.appspot.com/o/Books%2FCatcher.jpg?alt=media&token=dd8c6fab-4be1-495a-9b07-fe411e61718b'),
    (3, 'Great Expectations', 'Self-Help','Charles Dickens', '2008-11-11', 'https://firebasestorage.googleapis.com/v0/b/book-store-3ed32.appspot.com/o/Books%2FGreat.jpg?alt=media&token=e7e4ca9f-b789-4bed-b251-5278a821c99d'),
    (4, 'Tôi Thấy Hoa Vàng Trên Cỏ Xanh', 'Novel','Nguyễn Nhật Ánh', '2008-06-11-', 'https://firebasestorage.googleapis.com/v0/b/book-store-3ed32.appspot.com/o/Books%2FT%C3%B4i_th%E1%BA%A5y_hoa_v%C3%A0ng_tr%C3%AAn_c%E1%BB%8F_xanh.jpg?alt=media&token=ef1fa26e-b44e-40c9-8656-288d16882401'),
    (5, 'One-Punch Man', 'Comic','Yusuke Murata, ONE', '2009-03-12', 'https://firebasestorage.googleapis.com/v0/b/book-store-3ed32.appspot.com/o/Books%2FOne%20punch%20man.jpg?alt=media&token=6094b6a0-1f73-42a1-81cc-0f0716e92e50'),
    (6, "The Silence of the Lambs", "Horror", "Thomas Harris", "1988-01-03", "https://firebasestorage.googleapis.com/v0/b/book-store-3ed32.appspot.com/o/Books%2FSilence.jpg?alt=media&token=bebbee82-c995-43d0-a617-ab752582127b"),
    (7, "Tết Ở Làng Địa Ngục", "Horror", "Thảo Trang", "2022-07-21", "https://firebasestorage.googleapis.com/v0/b/book-store-3ed32.appspot.com/o/Books%2FT%E1%BA%BFt.png?alt=media&token=738a1214-79a0-4db6-9c32-3f1256486dbc"),
    (8, "Dune", "Fantasy", "Frank Herbert", "1965-08-12", "https://firebasestorage.googleapis.com/v0/b/book-store-3ed32.appspot.com/o/Books%2FDune.jpg?alt=media&token=a666bd06-4600-4762-be03-a367f72be0a4");
    
Insert into booklette.review values
	(1, "0HR9Q9Y1HJQvm59UoL4UiJQFXIJ2", 5, "An absolute masterpiece of horror literature! 'Whispers in the Shadows' captivates from the first page, weaving a chilling narrative that keeps you on the edge of your seat. The author's command over suspense and the art of slowly revealing the horror lurking within is truly commendable. A must-read for any horror aficionado!", ""),
    (2, "NSKxVRDePid6gmNndNGeEmAjsvS2", 4, " 'Eclipsed Nightmare' is a gripping journey into the macabre, blending psychological horror with supernatural elements. The character development is solid, and the author skillfully builds tension throughout. While the ending left me wanting a bit more closure, the overall experience was spine-tingling and memorable.", ""),
    (1, "NSKxVRDePid6gmNndNGeEmAjsvS2", 3, " 'Crimson Veil' offers a unique take on the horror genre, introducing a captivating plot with unexpected twists. However, the pacing felt uneven at times, and some of the character motivations seemed unclear. While it had moments of genuine fright, it lacked the consistency to secure a higher rating.", ""),
	(5, "t4TBmyNG4hgrUWyM2eNFEYlAuFu2", 2, "Unfortunately, 'Spectral Echoes' fell short of expectations. The plot seemed convoluted, and the attempts at horror felt forced. The characters lacked depth, making it challenging to connect with their struggles. Overall, the execution failed to deliver the spine-chilling experience promised, resulting in a disappointing read.", ""),
    (3, "Wanlg9TtCkdvThrmCwheIeMlSHI3", 1, " 'Labyrinth of Despair' is a horror novel that misses the mark entirely. The narrative lacks coherence, and the attempt at creating fear feels more like a series of disconnected scenes rather than a cohesive story. The characters are one-dimensional, and the predictable plot twists make this a forgettable entry into the horror genre.", "");
    
Insert into booklette.wishlist values
	("0HR9Q9Y1HJQvm59UoL4UiJQFXIJ2", 1),
    ("0HR9Q9Y1HJQvm59UoL4UiJQFXIJ2", 5),
    ("NSKxVRDePid6gmNndNGeEmAjsvS2", 3),
    ("NSKxVRDePid6gmNndNGeEmAjsvS2", 2),
    ("Wanlg9TtCkdvThrmCwheIeMlSHI3", 4),
    ("Wanlg9TtCkdvThrmCwheIeMlSHI3", 8),
    ("t4TBmyNG4hgrUWyM2eNFEYlAuFu2", 7);
    
	
    

