# MusicApplication-SE114
Project: MusicApplication – SE114 
Board: [URL tới bảng Trello / Jira]  
Members:

Update kế hoạch fe:
Hướng đi :
- Ở màn home sẽ hiển thị bài hát nổi bật là thuộc phần song, hiển thị album và nghệ sĩ.Khi nhấn vào song thì ra player song, nhấn back ở trên bên trái thì ra phần miniplayer và màn hình hiện tại ví dụ home.
  
- Phần album khi nhấn vào thì sẽ hiển thị list song trong album đó, và có phần search song trong album.
  
- phần artist khi nhấn vào thì sẽ hiển thị list song của artist đó, đi kèm là phần search song artist và thêm một nút button follow để có thể theo dõi artist khi nhấn vào.
  
- Phần search type có thể search song, artist, album.
  
- Hiển thị thêm nút tạo playlist, sau khi tạo ra thì có một playlist trống, sau đó có thể thêm song vào playlist, và khả năng hiển thị bên phần relax tạm thời vì bên For you hiển thị khá nhiều, (tạm thời).
  
- Mỗi bài hát chứa tym hoặc download khi nhấn vào icon đó ở màn player thì sẽ lưu vào danh sách yêu thích hoặc tải về của song.
  
- Phần library hiển thị recently played, là khi người dùng cứ nhấn vào một bài hát thì bài hát đó hiển thị thành màn player thì có nghĩa đó là recently played. Và nó hiển thị một danh sách ở library.
  
- Ở library có các tab Liked song, download, khi nhấn vào hiển thị danh sách bài hát yêu thích hoặc tải về, nhấn vào bài hát đó thì hiển thị player phát nhạc, nếu nhấn vào nút back thì về miniplayer và trạng trước player.
  
- còn playlist khi nhấn vào hiển thị các playlist đã tạo. Nhấn vào playlist thì sẽ hiển thị thêm danh sách bài hát được thêm vào playlist, nhấn vào bài hát thì hiển thị player phát nhạc, nhấn vào back về miniplayer.
  
- Còn artist sẽ hiển thị những nghệ sĩ đã theo dõi ở màn home, nhấn vào nghệ sĩ hiển thị danh sách nhạc, nhán vào nhạc vào player phát nhạc và tương tự như trên.

- Thì mỗi thao tác đều phải call api từ be, ví dụ thêm bài hát vào danh sách yêu thích sau khi nhấn nút tym thì gọi addFavoriteSong từ be ví dụ vậy, bám theo làm. Phần notification và phần forgetpassword đã có các api thực hiện nhiệm vụ, Dương để ý phần đó để bám theo. không hiểu hỏi AI thêm vì a cũng chưa có ý tưởng cho phần này. Rồi thêm phần Update profile là Dương đảm nhận, còn tất cả phần trên của app là do Tuấn làm.

- deadline là 29/6.

