# LastAssignment
ToDoApp→src→ToDoApp.java
1.ロジックとデータの一体化:

 タスクデータ (Task クラス) が状態と振る舞いを自己完結しています。
 コントローラ (ToDoController) がロジックを管理し、ビューやモデルから独立しています。
 
2.対称性:

 各操作（追加、編集、削除、トグル）をコントローラ内で対称的に実装しています。
 
3.アーキテクチャ根底技法:

 MVCパターンを採用し、モデル（Task、TaskRepository）、ビュー（ToDoView）、コントローラ（ToDoController）を明確に分離しました。
