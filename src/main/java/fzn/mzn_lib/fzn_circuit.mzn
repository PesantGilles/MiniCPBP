predicate fzn_circuit_with_offset(array[int] of var int: x, int: offset);

predicate fzn_circuit(array [int] of var int: x) =
  fzn_circuit_with_offset(x,min(index_set(x)));
