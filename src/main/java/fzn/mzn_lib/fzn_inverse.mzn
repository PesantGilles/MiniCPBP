%-----------------------------------------------------------------------------%
% Constrains two arrays of int variables, 'f' and 'invf', to represent inverse functions.
%-----------------------------------------------------------------------------%
predicate fzn_inverse_with_offset(array [int] of var int: f, array [int] of var int: invf, int: offset);

predicate fzn_inverse(array [int] of var int: f, array [int] of var int: invf) =
  fzn_inverse_with_offset(f,invf,min(index_set(f)));


