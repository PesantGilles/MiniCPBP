predicate fzn_circuit_indexedFrom0(array[int] of var int: x);

predicate fzn_circuit(array [int] of var int: x) =
  if min(index_set(x)) != 0 then
    fzn_circuit_indexedFrom0([x[i]-min(index_set(x)) | i in index_set(x)])
  else
    fzn_circuit_indexedFrom0(x)
  endif;
