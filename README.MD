
#Team members: 
- Heng Kuang 
- Yuyang Fan 
- Yufeng Wang

#Logic
  
 - Search:
  Recursively create new tree when visiting a node and set the node as the root of the tree. Run search on the new tree.
  When the tree's root is a leaf node, find the value based on the key.
  
 - Insert
  Recursively create new tree when visiting a node and set the node as the root of the tree. Run insert on the new tree.
  When the tree's root is a leaf node, find the right location to insert the key/value. If the inserted node is overflowed,
  run helper function to split the node.
  
 - Delete
  Recursively create new tree when visiting a node and set the node as the root of the tree. Run delete on the new tree.
  When the tree's root is a leaf node, find the right location to delete the key. If the node is underflow after deletion,
  run helper function to restructure the tree recursively from bottom to top.
  
#Bug
 - Could not pass junit test3 because of failure to reset the real root to the merged top node when the original node is empty.
