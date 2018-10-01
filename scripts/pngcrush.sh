find -f . | grep .png | while read line; do pngcrush -ow -brute $line; done
