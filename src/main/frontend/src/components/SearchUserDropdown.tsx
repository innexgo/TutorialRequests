import React from 'react';

import AsyncSelect from 'react-select/async';
import { fetchApi } from '../utils/utils';

interface SearchUserDropdownProps {
  apiKey: ApiKey,
  userKind: string,
  setFn: (id: number) => void
}

type UserOption = {
  label: string,
  value: number
}

export default function SearchUserDropdown(props: SearchUserDropdownProps) {
  const promiseOptions = async function(input: string): Promise<UserOption[]> {
    const results = await fetchApi('user/?' + new URLSearchParams([
      ['partialUserName', `${input.toUpperCase()}`],
      ['userKind', props.userKind!],
      ['offset', '0'],
      ['count', '20'],
      ['apiKey', `${props.apiKey.key}`],
    ])) as User[];
    return results.map(x => {
      return {
        label: `${x.name} -- ${x.secondaryId}`,
        value: x.id
      } as UserOption
    });
  };

  const onChange = (opt:any) => {
    props.setFn((opt as UserOption).value)
  }


  return <AsyncSelect isClearable={true} onChange={onChange} loadOptions={promiseOptions} />
}
