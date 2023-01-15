# picture-sorter

Sort pictures from camera into folders by creation date.

Usage: `PictureSorter <source-dir> <target-dir> <prefix>`

For example, `java -jar picture-sorter.jar C:\Users\Joe\Pictures C:\Users\Joe\Pictures JOE`

Will move files from the source dir to a YYYY_MM_DD directory under the target dir and prepend with `prefix` in
order to identify where picture came from.

For example, `C:\Users\Jon\Pictures\image1.jpg` taken on 1/1/2022 would move to 
`C:\Users\Joe\Pictures\2022_01_01\JOE_image1.jpg`.

If the target file already exists with the same md5 hash, the file will not be moved and a warning will be printed.
