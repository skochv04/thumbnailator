# 🖼️ Thumbnailator - Async Image Processing

Image Compression Service, developed as part of the 5th semester of studies at AGH. Built with Spring WebFlux for the backend and ReactJS for the frontend, enabling fast generation of image thumbnails.

## 👥 Team Members:
- **Stas Kochevenko**  
- **Mateusz Lampert**
- **Katarzyna Lisiecka-Meller**

## 📌 Project Overview:
Thumbnailator is an asynchronous image processing service that enables users to upload, manage, and retrieve image thumbnails instantly. Built with Java Spring WebFlux, ReactJS, and Google Cloud, it ensures high performance and scalability.

Thumbnailator provides an efficient way to **compress and organize images** featuring:

✅ Multi-image upload (supports PNG/JPG formats)  
✅ Asynchronous processing of thumbnails in different sizes (small, medium, large)  
✅ Instant access to processed thumbnails without waiting  
✅ Full-size image preview and on-demand downloads  
✅ Folder-based organization (create, delete, and move images between folders)  
✅ Archive support (upload ZIP files, automatic extraction, and thumbnail generation)  
✅ Pagination/Infinite scroll for smooth navigation while browsing thumbnails

Built with Spring WebFlux and ReactJS, it ensures high performance and responsiveness.

**🛠️ Technologies Used:**
- **Backend**: Java + Spring WebFlux
- **Frontend**: ReactJS  
- **Database**: H2  
- **Cloud Services**: Google Cloud

[![baner](baner.png)](https://youtu.be/6R6HCzGG-Jo)

## 🚀 How to Run the simulation?

### 1️⃣ Clone the Repository
```bash
git clone https://github.com/skochv04/thumbnailator  
cd repository
```

### 2️⃣ Run the backend server
```bash
cd thumbnailator
.\gradlew bootRun
```

### 3️⃣ Run the frontend
```bash
cd thumbnailator/react-frontend
npm start
```
