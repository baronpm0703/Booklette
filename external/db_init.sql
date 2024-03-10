CREATE TABLE Account (
	UID text PRIMARY KEY,
	Fullname text,
	Phone text,
	Email text,
	Pass text,
	Address text,
	ShippingAddress text
);

CREATE TABLE PurchaseHistory (
	OrderId text PRIMARY KEY,
	CustomerID text,
	CreationDate Date,
	Status text,
	TotalSum bigint,
	PaymentMethod text,
	FOREIGN KEY (CustomerID) REFERENCES Account (UID)
);

CREATE TABLE Book (
	BookID text PRIMARY KEY,
	Name text,
	Genre text,
	Author text,
	ReleaseDate Date,
	Image text
);

CREATE TABLE OrderItem (
	OrderID text,
	ItemID text,
	Quantity SMALLINT,
	TotalSum bigint,
	PRIMARY KEY (OrderID, ItemID),
	FOREIGN KEY (OrderID) REFERENCES PurchaseHistory (OrderId),
	FOREIGN KEY (ItemID) REFERENCES Book (BookID)
);

CREATE TABLE Review (
	BookID text,
	UID text,
	Score SMALLINT,
	Text text,
	Image text,
	PRIMARY KEY (BookID, UID),
	FOREIGN KEY (BookID) REFERENCES Book (BookID),
	FOREIGN KEY (UID) REFERENCES Account (UID)
);

CREATE TABLE PersonalStore (
	OwnerID text,
	ItemID text,
	Price bigint,
	Sold int,
	Remain int,
	Status text,
	Discount text[],
	PRIMARY KEY (OwnerID, ItemID),
	FOREIGN KEY (OwnerID) REFERENCES Account (UID),
	FOREIGN KEY (ItemID) REFERENCES Book (BookID)
);

CREATE TABLE ReturnExchange (
	CustomerID text,
	StoreID text,
	ItemID text,
	ReturnDate Date,
	Reason text,
	PRIMARY KEY (CustomerID, StoreID, ReturnDate),
	FOREIGN KEY (CustomerID) REFERENCES Account (UID),
	FOREIGN KEY (StoreID, ItemID) REFERENCES PersonalStore (OwnerID, ItemID)
);

CREATE TABLE Wishlist (
	UID text,
	ItemID text,
	PRIMARY KEY (UID, ItemID),
	FOREIGN KEY (UID) REFERENCES Account (UID),
	FOREIGN KEY (ItemID) REFERENCES Book (BookID)
);

CREATE TABLE Blacklist (
	UID text,
	SuspectID text,
	Reason text,
	SuspendDate Date,
	PRIMARY KEY (UID, SuspectID),
	FOREIGN KEY (UID) REFERENCES Account (UID),
	FOREIGN KEY (SuspectID) REFERENCES Account (UID)
);

CREATE TABLE Discount (
	DiscountID text,
	DiscountName text,
	DiscountType SMALLINT,
	DiscountTypeValue text,
	Percent SMALLINT,
	StartDate Date,
	EndDate Date,
	PRIMARY KEY (DiscountID, DiscountName, DiscountType, DiscountTypeValue)
);

insert into account values 
	('t4TBmyNG4hgrUWyM2eNFEYlAuFu2', 'Võ Chánh Tín', '0948363775', 'vctin21@clc.fitus.edu.vn', '25d55ad283aa400af464c76d713c07ad', 'Chung cư quân khu gì đó', 'Chung quân khu gì đó'),
	('Wanlg9TtCkdvThrmCwheIeMlSHI3', 'Phan Thái Khang', '099471123', 'ptkhang21@clc.fitus.edu.vn', '25d55ad283aa400af464c76d713c07ad', 'Quận 7, TP.HCM', 'Quận 5, TP.HCM'),
	('NSKxVRDePid6gmNndNGeEmAjsvS2', 'Nguyễn Lâm Hải', '0948361174', 'npmbao21@clc.fitus.edu.vn', '25d55ad283aa400af464c76d713c07ad', '22 hẻm 182 Lý Thường Kiệt', ''),
	('0HR9Q9Y1HJQvm59UoL4UiJQFXIJ2', 'Nguyễn Phú Minh Bảo', '0948361174', 'npmbao21@clc.fitus.edu.vn', '25d55ad283aa400af464c76d713c07ad', '22 hẻm 182 Lý Thường Kiệt', ''),
	('4JHgg23sf24JG34235nGnfdfn213', 'Nguyễn Anh Khoa', '0958438585', 'nakhoa21@clc.fitus.edu.vn', '25d55ad283aa400af464c76d713c07ad', '213 AAAA', '');
	
insert into book values
	('BK001', 'Tuesday Mooney Talks to Ghosts', 'Novel', 'Kate Racculia', '1996-12-01', 'https://firebasestorage.googleapis.com/v0/b/book-store-3ed32.appspot.com/o/Books%2FTuesday.jpg?alt=media&token=6a578daf-af25-434e-9952-ffa673a37d62'),
	('BK002', 'The Catcher in the eyes', 'Novel', 'J. D. Salinger', '2003-02-01', 'https://firebasestorage.googleapis.com/v0/b/book-store-3ed32.appspot.com/o/Books%2FCatcher.jpg?alt=media&token=dd8c6fab-4be1-495a-9b07-fe411e61718b'),
	('BK003', 'Great Expectations', 'Self-Help', 'Charles Dickens', '2008-11-11', 'https://firebasestorage.googleapis.com/v0/b/book-store-3ed32.appspot.com/o/Books%2FGreat.jpg?alt=media&token=e7e4ca9f-b789-4bed-b251-5278a821c99d'),
	('BK004', 'Tôi Thấy Hoa Vàng Trên Cỏ Xanh', 'Comic', 'Nguyễn Nhật Ánh', '2008-06-11', 'https://firebasestorage.googleapis.com/v0/b/book-store-3ed32.appspot.com/o/Books%2FT%C3%B4i_th%E1%BA%A5y_hoa_v%C3%A0ng_tr%C3%AAn_c%E1%BB%8F_xanh.jpg?alt=media&token=ef1fa26e-b44e-40c9-8656-288d16882401'),
	('BK005', 'One-Punch Man', 'Horror', 'Yusuke Murata, ONE', '2009-03-12', 'https://firebasestorage.googleapis.com/v0/b/book-store-3ed32.appspot.com/o/Books%2FOne%20punch%20man.jpg?alt=media&token=6094b6a0-1f73-42a1-81cc-0f0716e92e50'),
	('BK006', 'The Silence of the Lambs', 'Horror', 'Thomas Harris', '1988-01-03', 'https://firebasestorage.googleapis.com/v0/b/book-store-3ed32.appspot.com/o/Books%2FSilence.jpg?alt=media&token=bebbee82-c995-43d0-a617-ab752582127b'),
	('BK007', 'Tết Ở Làng Địa Ngục', 'Fantasy', 'Thảo Trang', '2022-07-21', 'https://firebasestorage.googleapis.com/v0/b/book-store-3ed32.appspot.com/o/Books%2FT%E1%BA%BFt.png?alt=media&token=738a1214-79a0-4db6-9c32-3f1256486dbc'),
	('BK008', 'Dune', 'Fantasy', 'Frank Herbert', '1965-08-12', 'https://firebasestorage.googleapis.com/v0/b/book-store-3ed32.appspot.com/o/Books%2FDune.jpg?alt=media&token=a666bd06-4600-4762-be03-a367f72be0a4');
	
insert into wishlist values
	('0HR9Q9Y1HJQvm59UoL4UiJQFXIJ2', 'BK001'),
	('0HR9Q9Y1HJQvm59UoL4UiJQFXIJ2', 'BK005'),
	('NSKxVRDePid6gmNndNGeEmAjsvS2', 'BK003'),
	('NSKxVRDePid6gmNndNGeEmAjsvS2', 'BK002'),
	('Wanlg9TtCkdvThrmCwheIeMlSHI3', 'BK004'),
	('Wanlg9TtCkdvThrmCwheIeMlSHI3', 'BK008'),
	('t4TBmyNG4hgrUWyM2eNFEYlAuFu2', 'BK007'),
	('4JHgg23sf24JG34235nGnfdfn213', 'BK006'),
	('4JHgg23sf24JG34235nGnfdfn213', 'BK001');
	
insert into review values
	('BK001', '0HR9Q9Y1HJQvm59UoL4UiJQFXIJ2', 5, 'An absolute masterpiece of horror literature! "Whispers in the Shadows captivates from the first page", weaving a chilling narrative that keeps you on the edge of your seat. The author''s command over suspense and the art of slowly revealing the horror lurking within is truly commendable. A must-read for any horror aficionado!'),
	('BK002', 'NSKxVRDePid6gmNndNGeEmAjsvS2', 4, '"Eclipsed Nightmare" is a gripping journey into the macabre, blending psychological horror with supernatural elements. The character development is solid, and the author skillfully builds tension throughout. While the ending left me wanting a bit more closure, the overall experience was spine-tingling and memorable.'),
	('BK001', 'NSKxVRDePid6gmNndNGeEmAjsvS2', 3, '"Crimson Veil" offers a unique take on the horror genre, introducing a captivating plot with unexpected twists. However, the pacing felt uneven at times, and some of the character motivations seemed unclear. While it had moments of genuine fright, it lacked the consistency to secure a higher rating.'),
	('BK005', 't4TBmyNG4hgrUWyM2eNFEYlAuFu2', 2, 'Unfortunately, "Spectral Echoes" fell short of expectations. The plot seemed convoluted, and the attempts at horror felt forced. The characters lacked depth, making it challenging to connect with their struggles. Overall, the execution failed to deliver the spine-chilling experience promised, resulting in a disappointing read.'),
	('BK003', 'Wanlg9TtCkdvThrmCwheIeMlSHI3', 1, '"Labyrinth of Despair" is a horror novel that misses the mark entirely. The narrative lacks coherence, and the attempt at creating fear feels more like a series of disconnected scenes rather than a cohesive story. The characters are one-dimensional, and the predictable plot twists make this a forgettable entry into the horror genre.');

insert into purchasehistory values
	('ORD001', '4JHgg23sf24JG34235nGnfdfn213', '2024-01-01', 'Thành công', 90000, 'Momo'),
	('ORD002', 'NSKxVRDePid6gmNndNGeEmAjsvS2', '2023-12-21', 'Bị Huỷ', 100000, 'Banking'),
	('ORD003', 't4TBmyNG4hgrUWyM2eNFEYlAuFu2', '2024-02-05', 'Thành công', 49000, 'Momo'),
	('ORD004', '0HR9Q9Y1HJQvm59UoL4UiJQFXIJ2', '2023-11-14', 'Hoàn Trả Lại', 120000, 'COD'),
	('ORD005', 'Wanlg9TtCkdvThrmCwheIeMlSHI3', '2024-03-02', 'Đang Xử Lý', 100000, 'COD');
	
insert into orderitem values
	('ORD001', 'BK002', 2, 20000),
	('ORD001', 'BK003', 1, 50000),
	('ORD002', 'BK003', 2, 50000),
	('ORD003', 'BK007', 1, 24000),
	('ORD003', 'BK002', 1, 25000),
	('ORD004', 'BK005', 2, 60000),
	('ORD005', 'BK005', 2, 20000),
	('ORD005', 'BK002', 1, 20000),
	('ORD005', 'BK004', 1, 16000),
	('ORD005', 'BK006', 1, 24000);
	
insert into discount values
	('DSC001', 'Giảm giá theo tên', 1, 'vàng', 10, '2024-01-01', '2024-05-01'),
	('DSC002', 'Giảm giá theo tác giả', 2, 'Nguyễn Nhật Ánh', 20, '2024-02-02', '2024-06-06');
	
insert into personalstore values
	('t4TBmyNG4hgrUWyM2eNFEYlAuFu2', 'BK002', 20000, 4, 10, '', '{}'),
	('t4TBmyNG4hgrUWyM2eNFEYlAuFu2', 'BK003', 50000, 5, 34, '', '{}'),
	('4JHgg23sf24JG34235nGnfdfn213', 'BK002', 25000, 1, 50, '', '{}'),
	('4JHgg23sf24JG34235nGnfdfn213', 'BK005', 60000, 6, 15, '', '{"DSC001"}'),
	('4JHgg23sf24JG34235nGnfdfn213', 'BK007', 24000, 6, 15, '', '{}'),
	('4JHgg23sf24JG34235nGnfdfn213', 'BK004', 16000, 0, 15, '', '{}'),
	('4JHgg23sf24JG34235nGnfdfn213', 'BK006', 24000, 5, 66, '', '{}'),
	('NSKxVRDePid6gmNndNGeEmAjsvS2', 'BK005', 20000, 5, 77, '', '{}');