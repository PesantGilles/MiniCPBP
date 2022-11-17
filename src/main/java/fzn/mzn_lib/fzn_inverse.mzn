%-----------------------------------------------------------------------------%
% Constrains two arrays of int variables, 'f' and 'invf', to represent inverse functions.
%-----------------------------------------------------------------------------%

predicate fzn_inverse_indexedFrom0(array [int] of var int: f, array [int] of var int: invf);

predicate fzn_inverse(array [int] of var int: f, array [int] of var int: invf) =
  if min(index_set(f)) > 0 then
    fzn_inverse_indexedFrom0([f[i]-min(index_set(f)) | i in index_set(f)],[invf[i]-min(index_set(f)) | i in index_set(f)])
  else
    fzn_inverse_indexedFrom0(f,invf)
  endif;

