# MusicApplication-SE114
Project: MusicApplication – SE114 
Board: [URL tới bảng Trello / Jira]  
Members:

Update kế hoạch fe:
Hướng đi :
- Ở màn home sẽ hiển thị bài hát nổi bật là thuộc phần song, hiển thị album và nghệ sĩ.Khi nhấn vào song thì ra player song, nhấn back ở trên bên trái thì ra phần miniplayer và màn hình hiện tại ví dụ home.
  
- Phần album khi nhấn vào thì sẽ hiển thị list song trong album đó, và có phần search song trong album.
  
   ![image](https://github.com/user-attachments/assets/677a9ff6-323f-483a-96a7-cd0d4d1ae092)

  
- Phần artist khi nhấn vào thì sẽ hiển thị list song của artist đó, đi kèm là phần search song artist và thêm một nút button follow để có thể theo dõi artist khi nhấn vào. Làm phần hiển thị bth, không cuộn lên vì không quá quan trọng

   ![image](https://github.com/user-attachments/assets/b20cfd5a-76d7-462c-bc20-a89c234f6aff)

  
- Phần search type có thể search song, artist, album.
  
   ![image](https://github.com/user-attachments/assets/a228053e-4211-4359-bf45-3d8a309144ec)

  
- Hiển thị thêm nút tạo playlist, sau khi tạo ra thì có một playlist trống, sau đó có thể thêm song vào playlist, và khả năng hiển thị bên phần relax tạm thời vì bên For you hiển thị khá nhiều, (tạm thời).
  
- Mỗi bài hát chứa tym hoặc download khi nhấn vào icon đó ở màn player thì sẽ lưu vào danh sách yêu thích hoặc tải về của song.
  
   ![image](https://github.com/user-attachments/assets/ab4c7483-3482-4a35-ace6-9e65b6a038f8)

  
- Phần library hiển thị recently played, là khi người dùng cứ nhấn vào một bài hát thì bài hát đó hiển thị thành màn player thì có nghĩa đó là recently played. Và nó hiển thị một danh sách ở library.
   ![image](https://github.com/user-attachments/assets/12c0b1ed-0356-4f73-828f-70be4000a1d4)

  
- Ở library có các tab Liked song, download, khi nhấn vào hiển thị danh sách bài hát yêu thích hoặc tải về, nhấn vào bài hát đó thì hiển thị player phát nhạc, nếu nhấn vào nút back thì về miniplayer và trạng trước player.
  
   ![image](https://github.com/user-attachments/assets/2c12d545-3339-401d-bb6a-b2b3643ca09d)

  
- Còn playlist khi nhấn vào hiển thị các playlist đã tạo. Nhấn vào playlist thì sẽ hiển thị thêm danh sách bài hát được thêm vào playlist, nhấn vào bài hát thì hiển thị player phát nhạc, nhấn vào back về miniplayer.
  
  ![image](https://github.com/user-attachments/assets/f7552842-21e2-4f08-8b2f-81c0057c029a)
  ![image](https://github.com/user-attachments/assets/07ac1261-4855-4dbb-b8b5-a11437b53766)

- Còn artist sẽ hiển thị những nghệ sĩ đã theo dõi ở màn home, nhấn vào nghệ sĩ hiển thị danh sách nhạc, nhán vào nhạc vào player phát nhạc và tương tự như trên.

- Thì mỗi thao tác đều phải call api từ be, ví dụ thêm bài hát vào danh sách yêu thích sau khi nhấn nút tym thì gọi addFavoriteSong từ be ví dụ vậy, bám theo làm. Phần notification và phần forgetpassword đã có các api thực hiện nhiệm vụ, Dương để ý phần đó để bám theo. không hiểu hỏi AI thêm vì a cũng chưa có ý tưởng cho phần này. Rồi thêm phần Update profile là Dương đảm nhận, còn tất cả phần trên của app là do Tuấn làm.

- Deadline là 29/6.

